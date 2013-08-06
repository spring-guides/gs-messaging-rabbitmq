
What you'll build
-----------------
This guide walks you through the process of setting up a RabbitMQ AMQP server and then using it to publish and subscribe for messages with Spring.

What you'll need
----------------
 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Maven 3.0][mvn] or later

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[mvn]: http://maven.apache.org/download.cgi
 - RabbitMQ server (installation instructions below)

How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/gs-messaging-rabbitmq.git`
 - cd into `gs-messaging-rabbitmq/initial`.
 - Jump ahead to [Creating a RabbitMQ message receiver](#initial).

**When you're finished**, you can check your results against the code in `gs-messaging-rabbitmq/complete`.
[zip]: https://github.com/springframework-meta/gs-messaging-rabbitmq/archive/master.zip


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Maven](/guides/gs/maven/content) or [Building Java Projects with Gradle](/guides/gs/gradle/content).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Maven POM

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-messaging-rabbitmq</artifactId>
    <version>0.1.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>0.5.0.BUILD-SNAPSHOT</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.amqp</groupId>
            <artifactId>spring-rabbit</artifactId>
            <version>1.2.0.RELEASE</version>
        </dependency>
        <dependency>
        	<groupId>org.springframework</groupId>
        	<artifactId>spring-context</artifactId>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/libs-snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/libs-snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

This guide is using [Spring Boot's starter POMs](/guides/gs/spring-boot/content).

Note to experienced Maven users who are unaccustomed to using an external parent project: you can take it out later, it's just there to reduce the amount of code you have to write to get started.

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

`src/main/java/hello/Receiver.java`
```java
package hello;

public class Receiver {
	public void receiveMessage(String message) {
		System.out.println("Received <" + message + ">");
	}
}
```

The `Receiver` is a simple POJO that defines a method for receiving messages. When we register it to receive messages, you can name it anything you want.

Registering the listener and sending a message
----------------------------------------------

Spring Rabbit provides everything you need to send and receive messages with RabbitMQ. Specifically, you need to configure:

- A connection factory
- A message listener container
- A Rabbit template

You'll use `RabbitTemplate` to send messages, and you will register a `Receiver` with the message listener container to receive messages. The connection factory drives both, allowing them to connect to the RabbitMQ server. 

> **Note:** Because we didn't set up queues from the command-line, we also need `AmqpAdmin` to allow creation of dynamic queues.

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

This example sets up a `CachingConnectionFactory` to our locally run RabbitMQ broker. That connection factory is injected into both the message listener container and the Rabbit template.

The bean defined in the `listenerAdapter()` method is registered as a message listener in the container defines in `container()`. It will listen for messages on the "chat" queue. Since the `Receiver` class is a POJO, it needs to be wrapped in the `MessageListenerAdapter`, where we specify it to invoke `receiveMessage`.

> **Note:** JMS queues and AMQP queues have different semantics. For example, JMS queues route messages to only one consumer. AMQP queues can route messages to many consumers. For more see [Understanding AMQP]().

The connection factory and message listener container beans are all you need to listen for messages. To send a message, you'll also need a Rabbit template.

The `main()` method kicks off everything by creating a Spring application context. This will start the message listener container and start listening for messages. It then retrieves the `RabbitTemplate` from the application context, waits five seconds, and send a "Hello from RabbitMQ!" message on the "chat" queue. Finally, it closes the Spring application context and the application ends.

Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Add the following configuration to your existing Maven POM:

`pom.xml`
```xml
    <properties>
        <start-class>hello.Application</start-class>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

The `start-class` property tells Maven to create a `META-INF/MANIFEST.MF` file with a `Main-Class: hello.Application` entry. This entry enables you to run it with `mvn spring-boot:run` (or simply run the jar itself with `java -jar`).

The [Spring Boot maven plugin][spring-boot-maven-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ mvn package
```

[spring-boot-maven-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-maven-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/content) instead.

Run the service
-------------------
Run your service using the spring-boot plugin at the command line:

```sh
$ mvn spring-boot:run
```


You should see the following output:

    Sending message...
    Received <Hello from RabbitMQ!>

Summary
-------
Congratulations! You've just developed a simple publisher and subscriber application using Spring and RabbitMQ. There's [more you can do with Spring and RabbitMQ](http://static.springsource.org/spring-amqp/reference/html/quick-tour.html) than what is covered here, but this should provide a good start.

