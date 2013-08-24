This guide walks you through the process of setting up a RabbitMQ AMQP server that  publishes and subscribes to messages.

What you'll build
-----------------

You'll build an application that publishes a message using Spring AMQP's `RabbitTemplate` and subscribes to the message on a POJO using `MessageListenerAdapter`.

What you'll need
----------------
 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Gradle 1.7+][gradle] or [Maven 3.0+][mvn]

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gradle]: http://www.gradle.org/
[mvn]: http://maven.apache.org/download.cgi
 - RabbitMQ server (installation instructions below)

How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [Git][u-git]:
`git clone https://github.com/spring-guides/gs-messaging-rabbitmq.git`
 - cd into `gs-messaging-rabbitmq/initial`.
 - Jump ahead to [Create a RabbitMQ message receiver](#initial).

**When you're finished**, you can check your results against the code in `gs-messaging-rabbitmq/complete`.
[zip]: https://github.com/spring-guides/gs-messaging-rabbitmq/archive/master.zip
[u-git]: /understanding/Git


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Gradle](http://gradle.org) and [Maven](https://maven.apache.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Gradle](/guides/gs/gradle/) or [Building Java Projects with Maven](/guides/gs/maven).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello


### Create a Gradle build file
Below is the [initial Gradle build file](https://github.com/spring-guides/gs-messaging-rabbitmq/blob/master/initial/build.gradle). But you can also use Maven. The pom.xml file is included [right here](https://github.com/spring-guides/gs-messaging-rabbitmq/blob/master/initial/pom.xml).

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'

jar {
    baseName = 'gs-messaging-rabbitmq'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.springsource.org/libs-snapshot" }
}

dependencies {
    compile("org.springframework.amqp:spring-rabbit:1.2.0.RELEASE")
    compile("org.springframework:spring-context")
    compile("cglib:cglib:2.2.2")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.7'
}
```
    
    

This guide is using [Spring Boot's starter POMs](/guides/gs/spring-boot/).

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

`src/main/java/hello/Receiver.java`
```java
package hello;

public class Receiver {
    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
    }
}
```

The `Receiver` is a simple POJO that defines a method for receiving messages. When you register it to receive messages, you can name it anything you want.

Register the listener and send a message
----------------------------------------------

Spring AMQP's `RabbitTemplate` provides everything you need to send and receive messages with RabbitMQ. Specifically, you need to configure:

- A connection factory
- A message listener container
- A Rabbit template

You'll use `RabbitTemplate` to send messages, and you will register a `Receiver` with the message listener container to receive messages. The connection factory drives both, allowing them to connect to the RabbitMQ server. 

> **Note:** Because you didn't set up queues from the command line, you also need `AmqpAdmin` to allow creation of dynamic queues.

`src/main/java/hello/Application.java`
```java
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
        return new MessageListenerAdapter(new Receiver(), "receiveMessage");
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
```

This example sets up a `CachingConnectionFactory` to your locally run RabbitMQ broker. That connection factory is injected into both the message listener container and the Rabbit template.

The bean defined in the `listenerAdapter()` method is registered as a message listener in the container defined in `container()`. It will listen for messages on the "chat" queue. Because the `Receiver` class is a POJO, it needs to be wrapped in the `MessageListenerAdapter`, where you specify it to invoke `receiveMessage`.

> **Note:** JMS queues and AMQP queues have different semantics. For example, JMS queues route messages to only one consumer. AMQP queues can route messages to many consumers. For more, see [Understanding AMQP]().

The connection factory and message listener container beans are all you need to listen for messages. To send a message, you also need a Rabbit template.

The `main()` method starts that process by creating a Spring application context. This starts the message listener container, which will start listening for messages. It then retrieves the `RabbitTemplate` from the application context, waits five seconds, and sends a "Hello from RabbitMQ!" message on the "chat" queue. Finally, the container closes the Spring application context and the application ends.

Build an executable JAR
-----------------------
Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Below are the Gradle steps, but if you are using Maven, you can find the updated pom.xml [right here](https://github.com/spring-guides/gs-messaging-rabbitmq/blob/master/complete/pom.xml) and build it by typing `mvn clean package`.

Update your Gradle `build.gradle` file's `buildscript` section, so that it looks like this:

```groovy
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.BUILD-SNAPSHOT")
    }
}
```

Further down inside `build.gradle`, add the following to the list of applied plugins:

```groovy
apply plugin: 'spring-boot'
```
You can see the final version of `build.gradle` [right here]((https://github.com/spring-guides/gs-messaging-rabbitmq/blob/master/complete/build.gradle).

The [Spring Boot gradle plugin][spring-boot-gradle-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.
It also searches for the `public static void main()` method to flag as a runnable class.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ ./gradlew build
```

If you are using Gradle, you can run the JAR by typing:

```sh
$ java -jar build/libs/gs-messaging-rabbitmq-0.1.0.jar
```

If you are using Maven, you can run the JAR by typing:

```sh
$ java -jar target/gs-messaging-rabbitmq-0.1.0.jar
```

[spring-boot-gradle-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-tools/spring-boot-gradle-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/) instead.

Run the service
-------------------
If you are using Gradle, you can run your service at the command line this way:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-messaging-rabbitmq-0.1.0.jar
```

> **Note:** If you are using Maven, you can run your service by typing `mvn clean package && java -jar target/gs-messaging-rabbitmq-0.1.0.jar`.


You should see the following output:

    Sending message...
    Received <Hello from RabbitMQ!>

Summary
-------
Congratulations! You've just developed a simple publish-and-subscribe application with Spring and RabbitMQ. There's [more you can do with Spring and RabbitMQ](http://static.springsource.org/spring-amqp/reference/html/quick-tour.html) than what is covered here, but this should provide a good start.

