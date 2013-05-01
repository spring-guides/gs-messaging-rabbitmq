package messagingrabbitmq;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
	
	@Bean
	SimpleMessageListenerContainer container() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer() {{
			setConnectionFactory(connectionFactory());
		}};
		container.setQueueNames("chat");
		container.setMessageListener(new Receiver());
		return container;
	}

	@Bean
	RabbitTemplate template() {
		return new RabbitTemplate(connectionFactory());
	}

	@Bean
	CachingConnectionFactory connectionFactory() {
		return new CachingConnectionFactory("localhost");
	}

	// Needed to dynamically create queues on demand
	@Bean
	AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}
	
}
