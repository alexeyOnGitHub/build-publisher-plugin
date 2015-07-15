package org.jenkinsci.plugins.bpp.publisher;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * This Test Publisher only records the number of times publish() was called.
 * <p>
 * This class is NOT thread safe: we don't need this because it is currently only used in
 * single-threaded tests.
 */
public final class CountingPublisher implements Publisher {
    private int counter;

    @Override
    public void publish(String message) throws IOException, TimeoutException {
        counter++;
    }

    public int getCounter() {
        return counter;
    }
}
