package com.example.messagingrabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
public class Receiver {

    private CountDownLatch latch = new CountDownLatch(1);

    @RabbitListener(queues = MessagingRabbitmqApplication.queueName, messageConverter = "Jackson2JsonMessageConverter")
    public void receiveMessage(Data message) {
        System.out.println("Received <" + message.getMessage() + ">");
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

}
