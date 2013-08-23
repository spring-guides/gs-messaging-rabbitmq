<#assign project_id="gs-messaging-rabbitmq">
This guide walks you through the process of setting up a RabbitMQ AMQP server that  publishes and subscribes to messages.

What you'll build
-----------------

You'll build an application that publishes a message using Spring AMQP's `RabbitTemplate` and subscribes to the message on a POJO using `MessageListenerAdapter`.

What you'll need
----------------
 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>
 - RabbitMQ server (installation instructions below)

## <@how_to_complete_this_guide jump_ahead='Create a RabbitMQ message receiver'/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>


<@create_both_builds/>

<@bootstrap_starter_pom_disclaimer/>

### Install and run RabbitMQ

Before you can build your messaging application, you need to set up the server that will handle receiving and sending messages.

RabbitMQ is an AMQP server. The server is freely available at <http://www.rabbitmq.com/download.html>. You can download it manually, or if you are using a Mac with homebrew:

    $ brew install rabbitmq

Unpack the server and launch it with default settings.

    $ rabbitmq-server

You should see something like this:

```sh
              RabbitMQ 3.1.3. Copyright (C) 2007-2013 VMware, Inc.
  ##  ##      Licensed under the MPL.  See http://www.rabbitmq.com/
  ##  ##
  ##########  Logs: /usr/local/var/log/rabbitmq/rabbit@localhost.log
  ######  ##        /usr/local/var/log/rabbitmq/rabbit@localhost-sasl.log
  ##########
              Starting broker... completed with 6 plugins.
```

<a name="initial"></a>
Create a RabbitMQ message receiver
---------------------------------

With any messaging-based application, you need to create a receiver that will respond to published messages.

    <@snippet path="src/main/java/hello/Receiver.java" prefix="complete"/>

The `Receiver` is a simple POJO that defines a method for receiving messages. When you register it to receive messages, you can name it anything you want.

Register the listener and send a message
----------------------------------------------

Spring AMQP's `RabbitTemplate` provides everything you need to send and receive messages with RabbitMQ. Specifically, you need to configure:

- A connection factory
- A message listener container
- A Rabbit template

You'll use `RabbitTemplate` to send messages, and you will register a `Receiver` with the message listener container to receive messages. The connection factory drives both, allowing them to connect to the RabbitMQ server. 

> **Note:** Because you didn't set up queues from the command line, you also need `AmqpAdmin` to allow creation of dynamic queues.

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

This example sets up a `CachingConnectionFactory` to your locally run RabbitMQ broker. That connection factory is injected into both the message listener container and the Rabbit template.

The bean defined in the `listenerAdapter()` method is registered as a message listener in the container defined in `container()`. It will listen for messages on the "chat" queue. Because the `Receiver` class is a POJO, it needs to be wrapped in the `MessageListenerAdapter`, where you specify it to invoke `receiveMessage`.

> **Note:** JMS queues and AMQP queues have different semantics. For example, JMS queues route messages to only one consumer. AMQP queues can route messages to many consumers. For more, see [Understanding AMQP]().

The connection factory and message listener container beans are all you need to listen for messages. To send a message, you also need a Rabbit template.

The `main()` method starts that process by creating a Spring application context. This starts the message listener container, which will start listening for messages. It then retrieves the `RabbitTemplate` from the application context, waits five seconds, and sends a "Hello from RabbitMQ!" message on the "chat" queue. Finally, the container closes the Spring application context and the application ends.

<@build_an_executable_jar_mainhead/>
<@build_an_executable_jar_with_both/>

<@run_the_application_with_both/>

You should see the following output:

    Sending message...
    Received <Hello from RabbitMQ!>

Summary
-------
Congratulations! You've just developed a simple publish-and-subscribe application with Spring and RabbitMQ. There's [more you can do with Spring and RabbitMQ](http://static.springsource.org/spring-amqp/reference/html/quick-tour.html) than what is covered here, but this should provide a good start.

