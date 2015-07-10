package org.jenkinsci.plugins.jpp.publisher;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public final class RabbitMQPublisher implements Publisher {
    private final static Logger LOG = Logger.getLogger(RabbitMQPublisher.class.getName());

    private static final AMQP.BasicProperties NO_PROPERTIES = null;
    private static final String NO_ROUTING_KEY = "";

    private final ConnectionFactory factory = new ConnectionFactory();

    private final String exchangeName;

    public RabbitMQPublisher(String rabbitMqHost, int rabbitMqPort, String exchangeName) {
        factory.setHost(rabbitMqHost);
        factory.setPort(rabbitMqPort);
        this.exchangeName = exchangeName;
    }

    @Override
    public void publish(String message) throws IOException, TimeoutException {
        LOG.info("Publishing to Exchange '" + exchangeName + "' on host " + factory.getHost());
        Connection connection = factory.newConnection();
        try {
            Channel channel = connection.createChannel();
            channel.exchangeDeclarePassive(exchangeName);
            channel.basicPublish(exchangeName, NO_ROUTING_KEY, NO_PROPERTIES, message.getBytes());
            LOG.info(" [x] Sent '" + message + "'");

            channel.close();
        } finally {
            // closing connection will close all its channels, according to its Javadoc.
            connection.close();
        }
    }
}
