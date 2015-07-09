package org.jenkinsci.plugins.jpp;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.jpp.publisher.RabbitMQPublisher;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class BuildRunListener extends RunListener<Run> implements Describable<BuildRunListener> {

    private final static Logger LOG = Logger.getLogger(BuildRunListener.class.getName());
    private final static Integer QUEUE_SIZE = 10;

    private final Processor processor;


    public BuildRunListener() {
        final RabbitMQPublisher publisher = createRabbitMQPublisher();
        processor = new Processor(publisher, QUEUE_SIZE);
        processor.start();
    }

    private RabbitMQPublisher createRabbitMQPublisher() {
        final DescriptorImpl descriptor = getDescriptor();
        return new RabbitMQPublisher(descriptor.getRabbitMqServerName(),
                descriptor.getRabbitMqServerPort(), descriptor.getRabbitMqExchangeName());
    }

    @Override
    public DescriptorImpl getDescriptor() {
        // yes, apparently have to recreate the object each time. otherwise it does not get updates(???!!)
        return new DescriptorImpl();
    }

    @Override
    public void onStarted(Run run, TaskListener listener) {
    }

    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        boolean enabled = getDescriptor().isEnabled();
        if (enabled) {
            LOG.fine("plugin is enabled. processing: " + run.getFullDisplayName());

            try {
                processBuildCompletedEvent(run);
            } catch (Throwable e) {
                LOG.log(Level.SEVERE, "Exception while processing 'completed build' request:" + e.toString(), e);
            }
        } else {
            LOG.fine("onCompleted: plugin is NOT enabled. ignoring: " + run.getFullDisplayName());
        }
    }

    private void processBuildCompletedEvent(Run run) throws IOException, TimeoutException {
        final String message = BuildMessageBuilder.buildMessage(run);

        processor.setPublisher(createRabbitMQPublisher());
        boolean added = processor.addToOutgoingQueue(message);
        if (added) {
            LOG.info(run.getFullDisplayName() + " - Added to the local Jenkins queue to be processed in a separate thread.");
        } else {
            LOG.warning(run.getFullDisplayName() + " CANNOT add to the outgoing Jenkins queue, it is full. this build will be ignored");
        }
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<BuildRunListener> {

        private volatile boolean enabled;
        private volatile String rabbitMqServerName;
        private volatile int rabbitMqServerPort;
        private volatile String rabbitMqExchangeName;

        public DescriptorImpl() {
            load();
        }

        public String getDisplayName() {
            return "Jenkins Publish Plugin";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            enabled = formData.getBoolean("enabled");
            rabbitMqServerName = formData.getString("rabbitMqServerName");
            // we know it's a number because it passed validation
            rabbitMqServerPort = formData.getInt("rabbitMqServerPort");
            rabbitMqExchangeName = formData.getString("rabbitMqExchangeName");
            save();
            LOG.info("Config updated. TODO: restart processor with the new data");
            // TODO restart Processor with the new data?
            return super.configure(req, formData);
        }

        public FormValidation doCheckRabbitMqServerName(@QueryParameter String value) {
            if (value.trim().isEmpty()) {
                return FormValidation.error("RabbitMQ server name cannot be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckRabbitMqServerPort(@QueryParameter String value) {
            final String trimmedValue = value.trim();
            if (trimmedValue.isEmpty()) {
                return FormValidation.error("RabbitMQ server port cannot be empty");
            }
            try {
                Integer.parseInt(trimmedValue);
            } catch (NumberFormatException e) {
                return FormValidation.error("Please provide a number");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckRabbitMqExchangeName(@QueryParameter String value) {
            final String trimmedValue = value.trim();
            if (trimmedValue.isEmpty()) {
                return FormValidation.error("Please provide a valid existing RabbitMQ Exchange name.");
            }
            return FormValidation.ok();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getRabbitMqServerName() {
            return rabbitMqServerName;
        }

        public int getRabbitMqServerPort() {
            return rabbitMqServerPort;
        }

        public String getRabbitMqExchangeName() {
            return rabbitMqExchangeName;
        }
    }

}

