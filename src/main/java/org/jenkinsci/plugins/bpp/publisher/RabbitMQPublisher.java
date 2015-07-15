package org.jenkinsci.plugins.bpp.publisher;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.jenkinsci.plugins.bpp.RabbitMQConfiguration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public final class RabbitMQPublisher implements Publisher {
    private final static Logger LOG = Logger.getLogger(RabbitMQPublisher.class.getName());

    private static final AMQP.BasicProperties NO_PROPERTIES = null;
    private static final String NO_ROUTING_KEY = "";

    private final ConnectionFactory factory = new ConnectionFactory();

    private final String exchangeName;

    public RabbitMQPublisher(RabbitMQConfiguration config) {
        factory.setHost(config.getServerName());
        factory.setPort(config.getServerPort());
        factory.setUsername(config.getUserName());
        factory.setPassword(config.getPassword());
        this.exchangeName = config.getExchangeName();
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
