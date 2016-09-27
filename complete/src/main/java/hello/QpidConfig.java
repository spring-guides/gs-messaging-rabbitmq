package hello;

import com.google.common.io.Files;
import hello.Application;
import hello.Receiver;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;

@Configuration
@Profile("qpid")
public class QpidConfig {

    String amqpPort = "5672";

    String qpidHomeDir = "complete";
    String configFileName = "src/main/resources/qpid-config.json";

    @Bean
    Receiver receiver() {
        return new Receiver();
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    Broker broker() throws Exception {

        Broker broker=new Broker();
        broker.startup(brokerOptions());
        return broker;
    }

    @Bean
    BrokerOptions brokerOptions() {

        File tmpFolder= Files.createTempDir();

        //small hack, because userDir is not same when running Application and ApplicationTest
        //it leads to some issue locating the files after, so hacking it here
        String userDir=System.getProperty("user.dir").toString();
        if(!userDir.contains(qpidHomeDir)){
            userDir=userDir+File.separator+qpidHomeDir;
        }

        File file = new File(userDir);
        String homePath = file.getAbsolutePath();

        BrokerOptions brokerOptions=new BrokerOptions();

        brokerOptions.setConfigProperty("qpid.work_dir", tmpFolder.getAbsolutePath());
        brokerOptions.setConfigProperty("qpid.amqp_port",amqpPort);
        brokerOptions.setConfigProperty("qpid.home_dir", homePath);
        brokerOptions.setInitialConfigurationLocation(homePath + "/"+configFileName);

        return brokerOptions;
    }


    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(Application.queueName);
        container.setMessageListener(listenerAdapter);
        return container;
    }
}
