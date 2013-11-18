
package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Receiver {

	@Autowired
	AnnotationConfigApplicationContext context;

	public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
        this.context.close();
    }
}
