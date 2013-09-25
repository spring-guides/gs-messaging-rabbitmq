package hello;

import java.util.concurrent.CountDownLatch;

public class Receiver {

    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
    }
}
