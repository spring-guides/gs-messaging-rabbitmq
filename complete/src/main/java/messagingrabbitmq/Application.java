package messagingrabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class Application {

	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
		System.out.println("Waiting five seconds...");
		Thread.sleep(5000);
		RabbitTemplate template = ctx.getBean(RabbitTemplate.class);
		System.out.println("Sending message...");
		template.convertAndSend("chat", "Hello from RabbitMQ!");
	}
}
