package com.example.messagingrabbitmq;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {

    @Autowired
    private  RabbitTemplate rabbitTemplate;
    private final Receiver receiver;

    public Runner(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Sending message...");
        Data data = new Data();
        data.setMessage("Hello from RabbitMQ!");
        rabbitTemplate.convertAndSend(MessagingRabbitmqApplication.topicExchangeName, "foo.bar.anything", data);
        receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
    }

}
