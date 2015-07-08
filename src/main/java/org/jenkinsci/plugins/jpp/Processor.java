package org.jenkinsci.plugins.jpp;

import org.jenkinsci.plugins.jpp.publisher.RabbitMQPublisher;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

final class Processor {

    private final static Logger LOG = Logger.getLogger(Processor.class.getName());

    private static final int NUMBER_OF_EXECUTORS = 3;
    private static final String STOP_MESSAGE = "";

    private final ExecutorService service;

    /**
     * BlockingQueue is thread safe, so there is no need in using manual locks.
     */
    private final BlockingQueue<String> messageQueue;

    private final RabbitMQPublisher publisher;

    Processor(final RabbitMQPublisher publisher, int queueSize) {
        this.publisher = publisher;
        messageQueue = new ArrayBlockingQueue<>(queueSize);
        service = Executors.newFixedThreadPool(NUMBER_OF_EXECUTORS);
    }

    private class Processing implements Runnable {
        private final RabbitMQPublisher publisher;

        public Processing(RabbitMQPublisher publisher) {
            this.publisher = publisher;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final String message = messageQueue.take();
                    if (message.equals(STOP_MESSAGE)) {
                        break;
                    }
                    service.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                publisher.publish(message);
                            } catch (Exception e) {
                                LOG.severe("Exception while publishing message '" + message + "': " + e.toString());
                            }
                        }
                    });
                } catch (Exception e) {
                    LOG.severe("Exception while processing messages in the queue: " + e.toString());
                }
            }
        }
    }

    void start() {
        final Thread thread = new Thread(new Processing(publisher));
        thread.setDaemon(true);
        thread.start();
    }

    void stop() throws InterruptedException {
        messageQueue.put(STOP_MESSAGE);
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
    }

    /**
     * this is a fast operation to add the outgoing message to processing queue.
     * it is thread safe.
     */
    boolean addToOutgoingQueue(String message) {
        return messageQueue.offer(message);
    }

}
