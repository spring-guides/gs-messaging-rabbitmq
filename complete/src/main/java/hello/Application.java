package hello;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Application {

	@Bean
	CachingConnectionFactory connectionFactory() {
		return new CachingConnectionFactory("localhost");
	}

	@Bean
	SimpleMessageListenerContainer container(final CachingConnectionFactory connectionFactory) {
		return new SimpleMessageListenerContainer() {{
			setConnectionFactory(connectionFactory);
			setQueueNames("chat");
			setMessageListener(listenerAdapter());
		}};
	}
	
	@Bean
	MessageListenerAdapter listenerAdapter() {
		return new MessageListenerAdapter(new Receiver()) {{
			setDefaultListenerMethod("receiveMessage");
		}};
	}
	
	@Bean
	RabbitTemplate template(CachingConnectionFactory connectionFactory) {
		return new RabbitTemplate(connectionFactory);
	}

	// Needed to dynamically create queues on demand
	@Bean
	AmqpAdmin amqpAdmin(CachingConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}
	
	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
		System.out.println("Waiting five seconds...");
		Thread.sleep(5000);
		RabbitTemplate template = ctx.getBean(RabbitTemplate.class);
		System.out.println("Sending message...");
		template.convertAndSend("chat", "Hello from RabbitMQ!");
		ctx.close();
	}
}
