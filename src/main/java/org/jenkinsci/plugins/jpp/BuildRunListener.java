package org.jenkinsci.plugins.jpp;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.jpp.publisher.RabbitMQPublisher;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
        getDescriptor().addListener(new RabbitMqConfigurationChangeListener() {
            @Override
            public void onChanged(String rabbitMqServerName, int rabbitMqServerPort, String rabbitMqExchangeName) {
                LOG.info("RabbitMQ configuration was changed, updating the running thread. New values: server=" +
                        rabbitMqServerName + " port=" + rabbitMqServerPort + " exchange=" + rabbitMqExchangeName);
                LOG.info("processor = " + processor);
                processor.setPublisher(createRabbitMQPublisher());
            }
        });
    }

    private RabbitMQPublisher createRabbitMQPublisher() {
        final DescriptorImpl descriptor = getDescriptor();
        return new RabbitMQPublisher(descriptor.getRabbitMqServerName(),
                descriptor.getRabbitMqServerPort(), descriptor.getRabbitMqExchangeName());
    }

    @Override
    public DescriptorImpl getDescriptor() {
        // requesting an instance from Jenkins rather than creating a new copy,
        // otherwise our own copy won't get updates when config is updated in Jenkins Configuration page.
        return (DescriptorImpl) Jenkins.getInstance().getDescriptor(getClass());
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
        final boolean includePublishedTestResults = getDescriptor().isIncludePublishedTestResults();
        final String message = BuildMessageBuilder.buildMessage(run, includePublishedTestResults);

        boolean added = processor.addToOutgoingQueue(message);
        if (added) {
            LOG.info(run.getFullDisplayName() + " - Added to the local Jenkins queue to be processed in a separate thread.");
        } else {
            LOG.warning(run.getFullDisplayName() + " CANNOT add to the outgoing Jenkins queue, it is full. this build will be ignored");
        }
    }

    private interface RabbitMqConfigurationChangeListener {
        void onChanged(String rabbitMqServerName, int rabbitMqServerPort, String rabbitMqExchangeName);
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<BuildRunListener> {

        /**
         * thread-safe implementation to prevent ConcurrentModificationExceptions.
         */
        private final transient List<RabbitMqConfigurationChangeListener> listeners = new CopyOnWriteArrayList<>();

        private volatile boolean enabled;
        private volatile boolean includePublishedTestResults;
        private volatile String rabbitMqServerName;
        private volatile int rabbitMqServerPort;
        private volatile String rabbitMqExchangeName;

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Jenkins Publish Plugin";
        }

        void addListener(RabbitMqConfigurationChangeListener listener) {
            listeners.add(listener);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            enabled = formData.getBoolean("enabled");

            includePublishedTestResults = formData.getBoolean("includePublishedTestResults");

            processRabbitMqConfiguration(formData);
            save();
            return super.configure(req, formData);
        }

        private void processRabbitMqConfiguration(JSONObject formData) {
            final String newRabbitMqServerName = formData.getString("rabbitMqServerName");
            // we know it's a number because it passed validation
            final int newRabbitMqServerPort = formData.getInt("rabbitMqServerPort");
            final String newRabbitMqExchangeName = formData.getString("rabbitMqExchangeName");

            if (!
                    (newRabbitMqServerName.equals(rabbitMqServerName)
                            && newRabbitMqServerPort == rabbitMqServerPort
                            && newRabbitMqExchangeName.equals(rabbitMqExchangeName))) {
                // at least one parameter is changed
                rabbitMqServerName = newRabbitMqServerName;
                rabbitMqServerPort = newRabbitMqServerPort;
                rabbitMqExchangeName = newRabbitMqExchangeName;
                notifyListeners(rabbitMqServerName, rabbitMqServerPort, rabbitMqExchangeName);
            }
        }

        private void notifyListeners(String rabbitMqServerName, int rabbitMqServerPort, String rabbitMqExchangeName) {
            for (RabbitMqConfigurationChangeListener listener : listeners) {
                listener.onChanged(rabbitMqServerName, rabbitMqServerPort, rabbitMqExchangeName);
            }
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

        public FormValidation doTestConnection(@QueryParameter("rabbitMqServerName") final String rabbitMqServerName,
                                               @QueryParameter("rabbitMqServerPort") final int rabbitMqServerPort,
                                               @QueryParameter("rabbitMqExchangeName") final String rabbitMqExchangeName) {
            try {
                RabbitMQPublisher publisher = new RabbitMQPublisher(rabbitMqServerName, rabbitMqServerPort, rabbitMqExchangeName);
                publisher.publish("test from Jenkins Publisher Plugin");
                return FormValidation.ok("Success");
            } catch (Exception e) {
                return FormValidation.error("Cannot send test message: " + e.toString());
            }
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

        public boolean isIncludePublishedTestResults() {
            return includePublishedTestResults;
        }
    }

}

