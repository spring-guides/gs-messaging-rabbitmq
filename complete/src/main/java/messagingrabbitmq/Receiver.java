package messagingrabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class Receiver implements MessageListener {

	@Override
	public void onMessage(Message message) {
		System.out.println("Received <" + new String(message.getBody()) + ">");
	}

}
