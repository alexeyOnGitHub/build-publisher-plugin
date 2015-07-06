package org.jenkinsci.plugins.jpp.publisher;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public final class RabbitMQPublisher {
    private final static Logger LOG = Logger.getLogger(RabbitMQPublisher.class.getName());

    private final ConnectionFactory factory = new ConnectionFactory();

    private final String queueName;

    public RabbitMQPublisher(String rabbitMqHost, int rabbitMqPort, String queueName) {
        factory.setHost(rabbitMqHost);
        factory.setPort(rabbitMqPort);
        this.queueName = queueName;
    }

    public void publish(String message) throws IOException, TimeoutException {
        LOG.info("will publish to " + queueName);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicPublish("", queueName, null, message.getBytes());
        LOG.info(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}
