Getting Started: Messaging with RabbitMQ
========================================

This Getting Started guide will walk you through the process of setting up a RabbitMQ AMQP server and then using it to publish and subscribe for messages.

To help you get started, we've provided an initial project structure as well as the completed project for you in GitHub:

```sh
$ git clone https://github.com/springframework-meta/gs-messaging-rabbitmq
```

In the `initial` folder, you'll find a bare project, ready for you to copy-n-paste code snippets from this document. In the `complete` folder, you'll find the complete project code.

Before we code a publisher and subscriber, there is some initial project setup that's required. Or, you can skip straight to the [fun part](#setting-up-a-rabbitmq-server).

Selecting Dependencies
----------------------
The sample in this Getting Started Guide will leverage Spring AMQP and Spring Framework while running a RabbitMQ server. Therefore, the following dependencies are needed in the project's build configuration:

- org.springframework:spring-context:3.2.2.RELEASE
- org.springframework.amqp:spring-rabbit:1.1.4.RELEASE

Refer to the [Gradle Getting Started Guide]() or the [Maven Getting Started Guide]() for details on how to include these dependencies in your build.

Configuring a runnable application
----------------------------------

First of all, we need to create a basic runnable application.

```java
package messagingrabbitmq;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {

	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
	}
}
```

This application will load an application context from the `Config` class. Let's define that next.

```java
package messagingrabbitmq;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
		
}
```

Our configuration can't get much simpler. We essentially don't have any components defined yet.

To finish setting things up, let's configure some logging options in **log4j.properties**.

```text
# Set root logger level to DEBUG and its only appender to A1.
log4j.rootLogger=WARN, A1

# A1 is set to be a ConsoleAppender.
log4j.appender.A1=org.apache.log4j.ConsoleAppender

# A1 uses PatternLayout.
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n

log4j.category.org.springframework=INFO
```

Now we can run out bare application.

```sh
$ ./gradlew run
```

We should see something like this:

```sh
May 01, 2013 4:20:19 PM org.springframework.context.support.AbstractApplicationContext prepareRefresh
INFO: Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@52d03a00: startup date [Wed May 01 16:20:19 CDT 2013]; root of context hierarchy
May 01, 2013 4:20:19 PM org.springframework.beans.factory.support.DefaultListableBeanFactory preInstantiateSingletons
INFO: Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@58e90d6d: defining beans [org.springframework.context.annotation.internalConfigurationAnnotationProcessor,org.springframework.context.annotation.internalAutowiredAnnotationProcessor,org.springframework.context.annotation.internalRequiredAnnotationProcessor,org.springframework.context.annotation.internalCommonAnnotationProcessor,config,org.springframework.context.annotation.ConfigurationClassPostProcessor.importAwareProcessor]; root of factory hierarchy
```

With all this setup, let's dive into building a real messaging application.

Setting up a RabbitMQ server
-------------------------
Before we can build our messaging application, we need to set up the server that will handle receiving and sending messages.

RabbitMQ is an AMQP server. The server is freely available at <http://www.rabbitmq.com/download.html>. You can manually download it, or if happen to be using a Mac with homebrew:

```sh
$ brew install rabbitmq
```

Once you have unpacked it, you can launch it with default settings.

```sh
$ rabbitmq-server
```

You should expect something like this:

```sh
** Found 0 name clashes in code paths 

+---+   +---+
|   |   |   |
|   |   |   |
|   |   |   |
|   +---+   +-------+
|                   |
| RabbitMQ  +---+   |
|           |   |   |
|   v3.0.4  +---+   |
|                   |
+-------------------+
AMQP 0-9-1 / 0-9 / 0-8
Copyright (C) 2007-2013 VMware, Inc.
Licensed under the MPL.  See http://www.rabbitmq.com/

node           : rabbit@localhost
app descriptor : /usr/local/Cellar/rabbitmq/3.0.4/ebin/rabbit.app
home dir       : /Users/gturnquist
config file(s) : (none)
cookie hash    : OMGfpo0fQY5lXVkfrbVvUA==
log            : /usr/local/var/log/rabbitmq/rabbit@localhost.log
sasl log       : /usr/local/var/log/rabbitmq/rabbit@localhost-sasl.log
database dir   : /usr/local/var/lib/rabbitmq/mnesia/rabbit@localhost
erlang version : 5.9.3.1

-- rabbit boot start
starting file handle cache server                                     ...done
starting worker pool                                                  ...done
starting database                                                     ...done
starting database sync                                                ...done
starting codec correctness check                                      ...done
-- external infrastructure ready
starting plugin registry                                              ...done
starting auth mechanism cr-demo                                       ...done
starting auth mechanism amqplain                                      ...done
starting auth mechanism plain                                         ...done
starting statistics event manager                                     ...done
starting logging server                                               ...done
starting exchange type direct                                         ...done
starting exchange type fanout                                         ...done
starting exchange type headers                                        ...done
starting exchange type topic                                          ...done
-- kernel ready
starting alarm handler                                                ...done
starting node monitor                                                 ...done
starting cluster delegate                                             ...done
starting guid generator                                               ...done
starting memory monitor                                               ...done
-- core initialized
starting management agent                                             ...done
starting HA policy validation                                         ...done
starting policy parameters                                            ...done
starting exchange, queue and binding recovery                         ...done
starting configured definitions                                       ...done
starting empty DB check                                               ...done
starting mirror queue slave sup                                       ...done
starting adding mirrors to queues                                     ...done
-- message delivery logic ready
starting error log relay                                              ...done
starting background garbage collection                                ...done
starting networking                                                   ...done
starting direct client                                                ...done
starting notify cluster nodes                                         ...done

broker running

-- plugins running
amqp_client                                                             3.0.4
mochiweb                                            2.3.1-rmq3.0.4-gitd541e9a
rabbitmq_management                                                     3.0.4
rabbitmq_management_agent                                               3.0.4
rabbitmq_web_dispatch                                                   3.0.4
webmachine                                          1.9.1-rmq3.0.4-git52e62bc
```

Creating a RabbitMQ message receiver
---------------------------------
With any messaging-based application, we need to create a receiver that will respond to published messages.

```java
package messagingrabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class Receiver implements MessageListener {

	@Override
	public void onMessage(Message message) {
		System.out.println("Received <" + new String(message.getBody()) + ">");
	}

}
```

We simply implement the `MessageListener` interface to handle a message when it gets pushed to us. Later on, we'll show how to register our listener. In this case, we are simply printing out the content of the message. 

Publishing a message
--------------------
We've coded a message receiver. Now let's alter our startup `main` so that it not only creates an application context, but then sends out a single message.

```java
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
```

Here we see the same code from earlier that created an annotation-based application context. Next we ask it to sleep for five seconds, ensuring that everything has started up successfully. Then we extract a `RabbitTemplate` from the context and use it to publish a string message on queue `chat`.

The method `convertAndSend` does the leg work for us of marshalling the queue and message into bytes and pushing them out to the RabbitMQ server we started earlier, using Spring's familiar template pattern, very similar to `JmsTemplate`.

Wiring up all the components
----------------------------
We have a sender and a receiver. We just need to wire up the components in the middle that will make it all happen. To do that, we need to add some components to `Config`.

```java
package messagingrabbitmq;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
	
	@Bean
	SimpleMessageListenerContainer container() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer() {{
			setConnectionFactory(connectionFactory());
		}};
		container.setQueueNames("chat");
		container.setMessageListener(new Receiver());
		return container;
	}

	@Bean
	RabbitTemplate template() {
		return new RabbitTemplate(connectionFactory());
	}

	@Bean
	CachingConnectionFactory connectionFactory() {
		return new CachingConnectionFactory("localhost");
	}

	// Needed to dynamically create queues on demand
	@Bean
	AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}
	
}
```

Here we can see four key components.

- `container()` creates an instance of `SimpleMessageListenerContainer`. This is very similar to Spring Framework's JMS-oriented `DefaultMessageListenerContainer`. The container hooks up to the RabbitMQ server and listens for any published messages. Next it sets the queue name it will respond to as well as registers an instance of `Receiver`, to which it will push any new messages.
- `template()` creates an instance of `RabbitTemplate`, providing us the means to publish messages.
- `connectionFactory()` is responsible for creating a `CachingConnectionFactory`, needed for both the `SimpleMessageListenerContainer` and `RabbitTemplate` to reach the RabbitMQ server.
- `amqpAdmin()` creates a bean that makes it possible to create queues dynamically, saving us the administrative step of creating the `chat` queue manually.

Building and Running our Application
------------------------------------

With our message sender and receiver coded, along with wiring up components in between, we can run our application.

```sh
$ ./gradlew run
```

We should see something like:

```sh
May 01, 2013 4:27:39 PM org.springframework.context.support.AbstractApplicationContext prepareRefresh
INFO: Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@3806846c: startup date [Wed May 01 16:27:39 CDT 2013]; root of context hierarchy
May 01, 2013 4:27:40 PM org.springframework.beans.factory.support.DefaultListableBeanFactory preInstantiateSingletons
INFO: Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@22c6bb6c: defining beans [org.springframework.context.annotation.internalConfigurationAnnotationProcessor,org.springframework.context.annotation.internalAutowiredAnnotationProcessor,org.springframework.context.annotation.internalRequiredAnnotationProcessor,org.springframework.context.annotation.internalCommonAnnotationProcessor,config,org.springframework.context.annotation.ConfigurationClassPostProcessor.importAwareProcessor,template,connectionFactory,container,amqpAdmin]; root of factory hierarchy
May 01, 2013 4:27:40 PM org.springframework.context.support.DefaultLifecycleProcessor$LifecycleGroup start
INFO: Starting beans in phase 2147483647
Waiting five seconds...
Sending message...
Received <Hello from RabbitMQ!>
```

Next Steps
----------
Congratulations! You just setup RabbitMQ and built a message-based application.

- Read [Spring AMQP's quick start](http://static.springsource.org/spring-amqp/reference/html/quick-tour.html)
