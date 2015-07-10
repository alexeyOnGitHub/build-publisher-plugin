package org.jenkinsci.plugins.jpp.publisher;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Publisher {
    /**
     * Potentially blocking operation to publish the message.
     */
    void publish(String message) throws IOException, TimeoutException;
}
