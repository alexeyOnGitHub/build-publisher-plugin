package org.jenkinsci.plugins.jpp.publisher;

import org.junit.Test;

public class RabbitMQPublisherIT {

    private static final String RABBIT_MQ_HOST = "localhost";
    private static final int RABBIT_MQ_PORT = 5672;
    private static final String EXCHANGE_NAME = "jenkins-test-exchange";

    // TODO This fails because the exchange must exist before running it.
    // modify the tests to auto-create it.
    @Test
    public void canPublishToRabbitMQ() throws Exception {
        // for now just check that there are no exceptions when publishing
        final RabbitMQPublisher publisher = new RabbitMQPublisher(RABBIT_MQ_HOST, RABBIT_MQ_PORT, EXCHANGE_NAME);
        publisher.publish("sample message");
    }
}