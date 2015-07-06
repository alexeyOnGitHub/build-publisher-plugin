package org.jenkinsci.plugins.jpp;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.jpp.publisher.RabbitMQPublisher;
import org.kohsuke.stapler.DataBoundConstructor;
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
        LOG.log(Level.INFO, "JPP BuildRunListener: starting...");
        final DescriptorImpl descriptor = getDescriptor();
        final RabbitMQPublisher publisher = new RabbitMQPublisher(descriptor.getRabbitMqServerName(), descriptor.getRabbitMqServerPort(), descriptor.getRabbitMqQueueName());
        processor = new Processor(publisher, QUEUE_SIZE);
        processor.start();
        LOG.log(Level.INFO, "JPP BuildRunListener: started.");
    }

    @Override
    public DescriptorImpl getDescriptor() {
        // yes, apparently have to recreate the object each time. otherwise it does not get updates!
        return new DescriptorImpl();
    }

    @Override
    public void onStarted(Run run, TaskListener listener) {
    }

    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        boolean enabled = getDescriptor().isEnabled();
        if (enabled) {
            LOG.log(Level.FINE, "JPP: is enabled. starting processing");

            try {
                processBuildCompletedEvent(run);
            } catch (Throwable e) {
                LOG.log(Level.WARNING, "Exception while processing 'completed build' request:" + e.toString(), e);
            }
        } else {
            try {
                processor.stop();
            } catch (InterruptedException e) {
                LOG.log(Level.WARNING, "Exception while stopping Processor instance:" + e.toString(), e);
            }
            LOG.log(Level.INFO, " Jenkins Publisher Plugin is not enabled");
        }
    }

    private void processBuildCompletedEvent(Run run) throws IOException, TimeoutException {
        final Job job = run.getParent();

        boolean added = processor.addToOutgoingQueue(getJenkinsRootUrl(), job.getDisplayName(), run.getNumber());
        if (added) {
            LOG.log(Level.INFO, run.getFullDisplayName() + " - Added to the Queue");
        } else {
            LOG.log(Level.WARNING, run.getFullDisplayName() + " CANNOT add to the outgoing queue, it is full. this build will be ignored");
        }
    }

    private static String getJenkinsRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<BuildRunListener> {

        private volatile boolean enabled;
        private volatile String rabbitMqServerName;
        private volatile int rabbitMqServerPort;
        private volatile String rabbitMqQueueName;

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
            rabbitMqQueueName = formData.getString("rabbitMqQueueName");
            save();

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

        public FormValidation doCheckRabbitMqQueueName(@QueryParameter String value) {
            final String trimmedValue = value.trim();
            if (trimmedValue.isEmpty()) {
                return FormValidation.error("Queue name cannot be empty");
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

        public String getRabbitMqQueueName() {
            return rabbitMqQueueName;
        }
    }

}

