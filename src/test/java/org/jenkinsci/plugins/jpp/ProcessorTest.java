package org.jenkinsci.plugins.jpp;

import org.jenkinsci.plugins.jpp.publisher.CountingPublisher;
import org.jenkinsci.plugins.jpp.publisher.DevNullPublisher;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ProcessorTest {

    private static final int QUEUE_SIZE_3 = 3;

    @Test
    public void cannotStartProcessorTwice() {
        Processor processor = new Processor(new DevNullPublisher(), QUEUE_SIZE_3);
        processor.start();
        try {
            processor.start();
            fail("Must have failed");
        } catch (IllegalStateException e) {
            System.out.println("Got expected IllegalStateException e.");
        }
    }

    @Test
    public void nonStartedProcessorAccumulatesMessagesInQueue() {
        final Processor processor = new Processor(new DevNullPublisher(), QUEUE_SIZE_3);
        assertThat(processor.getQueueSize()).isEqualTo(0);
        processor.addToOutgoingQueue("1");
        assertThat(processor.getQueueSize()).isEqualTo(1);
        processor.addToOutgoingQueue("2");
        processor.addToOutgoingQueue("3");
        assertThat(processor.getQueueSize()).isEqualTo(3);
    }

    @Test
    public void nonStartedProcessorDoesNotAcceptMoreThanQueueSize() {
        final Processor processor = new Processor(new DevNullPublisher(), QUEUE_SIZE_3);
        assertThat(processor.addToOutgoingQueue("will be accepted 1")).isTrue();
        assertThat(processor.addToOutgoingQueue("will be accepted 2")).isTrue();
        assertThat(processor.addToOutgoingQueue("will be accepted 3")).isTrue();
        assertThat(processor.addToOutgoingQueue("will NOT be accepted")).isFalse();
        assertThat(processor.getQueueSize()).isEqualTo(3);
    }

    // if it does not finish in N msec, it must be broken
    @Test(timeout = 5000)
    public void messagesAreConsumed() throws InterruptedException {
        final CountingPublisher publisher = new CountingPublisher();
        final Processor processor = new Processor(publisher, QUEUE_SIZE_3);
        processor.start();
        processor.addToOutgoingQueue("1");
        processor.addToOutgoingQueue("2");
        processor.addToOutgoingQueue("3");
        while (publisher.getCounter() != QUEUE_SIZE_3) {
            Thread.sleep(50);
        }
        assertThat(processor.getQueueSize()).isEqualTo(0);
    }

}