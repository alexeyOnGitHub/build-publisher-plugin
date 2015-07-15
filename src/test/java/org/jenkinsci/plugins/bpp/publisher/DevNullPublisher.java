package org.jenkinsci.plugins.bpp.publisher;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * This Publisher ignores any messages it receives.
 */
public final class DevNullPublisher implements Publisher {
    @Override
    public void publish(String message) throws IOException, TimeoutException {
        // ignore
    }
}
