package org.jenkinsci.plugins.jpp.publisher;

import org.jenkinsci.plugins.jpp.RabbitMQConfiguration;
import org.junit.Test;

public class RabbitMQPublisherIT {

    private static final String RABBIT_MQ_HOST = "10.86.67.26";
    private static final int RABBIT_MQ_PORT = 5672;
    private static final String EXCHANGE_NAME = "jenkins-test-exchange";
    private static final String USER_NAME = "test";
    private static final String PASSWORD = "test";

    @Test
    public void canPublishToRabbitMQ() throws Exception {
        // for now just check that there are no exceptions when publishing
        final RabbitMQConfiguration config = new RabbitMQConfiguration(RABBIT_MQ_HOST, RABBIT_MQ_PORT, USER_NAME, PASSWORD, EXCHANGE_NAME);
        final RabbitMQPublisher publisher = new RabbitMQPublisher(config);
        publisher.publish("sample message");
    }
}