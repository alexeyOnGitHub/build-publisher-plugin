package org.jenkinsci.plugins.jpp.publisher;

import org.junit.Test;

public class RabbitMQPublisherIT {

    private static final String RABBIT_MQ_HOST = "localhost";
    private static final int RABBIT_MQ_PORT = 5672;
    private static final String QUEUE_NAME = "test-queue";

    @Test
    public void canPublishToRabbitMQ() throws Exception {
        // for now just check that there are no exceptions when publishing
        final RabbitMQPublisher publisher = new RabbitMQPublisher(RABBIT_MQ_HOST, RABBIT_MQ_PORT, QUEUE_NAME);
        publisher.publish("sample message");
    }
}