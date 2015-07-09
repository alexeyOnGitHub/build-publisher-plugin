package org.jenkinsci.plugins.jpp;

import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.logging.Logger;

final class BuildMessageBuilder {
    private final static Logger LOG = Logger.getLogger(BuildMessageBuilder.class.getName());

    static String buildMessage(Run run) {
        final Job job = run.getParent();
        final Result result = run.getResult();
        // can be null for builds in progress! oh well...
        final String resultText = result == null ? "" : result.toString();

        final JsonStringBuilder builder = new JsonStringBuilder()
                .jenkinsUrl(getJenkinsRootUrl())
                .jobName(job.getDisplayName())
                .buildNumber(run.getNumber())
                .result(resultText);

        ParametersAction parameters = run.getAction(ParametersAction.class);
        if (parameters!=null) {
            for (ParameterValue p : parameters) {
                builder.parameter(p.getName(), p.getValue());
            }
        }

        return builder.buildString();
    }

    private static String getJenkinsRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }
}
