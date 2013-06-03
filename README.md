Getting Started: Messaging with RabbitMQ
========================================

What you'll build
-----------------
This guide walks you through the process of setting up a RabbitMQ AMQP server and then using it to publish and subscribe for messages with Spring.


What you'll need
----------------
- About 15 minutes
- {!include#prereq-editor-jdk-buildtools}
- RabbitMQ server (installation instructions below)

## {!include#how-to-complete-this-guide}


<a name="scratch"></a>
Set up the project
------------------

{!include#build-system-intro}

{!include#create-directory-structure-hello}

### Create a Maven POM

{!include#maven-project-setup-options}

    {!include:complete/pom.xml}

{!include#bootstrap-starter-pom-disclaimer}

### Installing and running RabbitMQ

Before we can build our messaging application, we need to set up the server that will handle receiving and sending messages.

RabbitMQ is an AMQP server. The server is freely available at <http://www.rabbitmq.com/download.html>. You can manually download it, or if happen to be using a Mac with homebrew:

    $ brew install rabbitmq

Once you have unpacked it, launch it with default settings.

    $ rabbitmq-server

You should expect something like this:

                  RabbitMQ 3.1.0. Copyright (C) 2007-2013 VMware, Inc.
      ##  ##      Licensed under the MPL.  See http://www.rabbitmq.com/
      ##  ##
      ##########  Logs: /usr/local/var/log/rabbitmq/rabbit@localhost.log
      ######  ##        /usr/local/var/log/rabbitmq/rabbit@localhost-sasl.log
      ##########
                  Starting broker... completed with 6 plugins.


<a name="initial"></a>
Creating a RabbitMQ message receiver
---------------------------------

With any messaging-based application, we need to create a receiver that will respond to published messages.

    {!include:complete/src/main/java/hello/Receiver.java}

The `Receiver` is a simple POJO that defines a method for receiving messages. When we register it to receive messages, you can name it anything you want.

Registering the listener and sending a message
----------------------------------------------

Spring Rabbit provides everything you need to send and receive messages with RabbitMQ. Specifically, you need to configure:

- A connection factory
- A message listener container
- A Rabbit template

You'll use `RabbitTemplate` to send messages, and you will register a `Receiver` with the message listener container to receive messages. The connection factory drives both, allowing them to connect to the RabbitMQ server. 

> **Note:** Because we didn't set up queues from the command-line, we also need `AmqpAdmin` to allow creation of dynamic queues.

    {!include:complete/src/main/java/hello/Application.java}

This example sets up a `CachingConnectionFactory` to our locally run RabbitMQ broker. That connection factory is injected into both the message listener container and the Rabbit template.

The bean defined in the `listenerAdapter()` method is registered as a message listener in the container defines in `container()`. It will listen for messages on the "chat" queue. Since the `Receiver` class is a POJO, it needs to be wrapped in the `MessageListenerAdapter`, where we specify it to invoke `receiveMessage`.

> **Note:** JMS queues and AMQP queues have different semantics. For example, JMS queues route messages to only one consumer. AMQP queues can route messages to many consumers. For more see [Understanding AMQP]().

The connection factory and message listener container beans are all you need to listen for messages. To send a message, you'll also need a Rabbit template.

The `main()` method kicks off everything by creating a Spring application context. This will start the message listener container and start listening for messages. It then retrieves the `RabbitTemplate` from the application context, waits five seconds, and send a "Hello from RabbitMQ!" message on the "chat" queue. Finally, it closes the Spring application context and the application ends.

## {!include#build-an-executable-jar}

Run the application
-------------------
Run your application with `java -jar` at the command line:

    java -jar target/gs-messaging-rabbitmq-complete-0.1.0.jar

You should see the following output:

    Sending message...
    Received <Hello from RabbitMQ!>

Summary
-------
Congratulations! You've just developed a simple publisher and subscriber application using Spring and RabbitMQ. There's [more you can do with Spring and RabbitMQ](http://static.springsource.org/spring-amqp/reference/html/quick-tour.html) than what is covered here, but this should provide a good start.
