package org.jenkinsci.plugins.jpp;

import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class knows how to retrieve information from Jenkins object models for
 * Build and Jenkins JUnit results.
 */
final class BuildMessageBuilder {
    private final static Logger LOG = Logger.getLogger(BuildMessageBuilder.class.getName());

    static String buildMessage(Run run, boolean includePublishedTestResults) {
        final Job job = run.getParent();
        final Result result = run.getResult();
        // can be null for builds in progress! oh well...
        final String resultText = result == null ? "" : result.toString();

        final JsonStringBuilder builder = new JsonStringBuilder()
                .jenkinsUrl(getJenkinsRootUrl())
                .jobName(job.getDisplayName())
                .buildNumber(run.getNumber())
                .result(resultText)
                .id(run.getId());

        attachParametersIfPresent(builder, run);
        if (includePublishedTestResults) {
            attachPublishedTestResultsIfPresent(builder, run);
        }

        return builder.buildString();
    }

    private static void attachParametersIfPresent(JsonStringBuilder builder, Run run) {
        final ParametersAction parameters = run.getAction(ParametersAction.class);
        if (parameters != null) {
            for (ParameterValue p : parameters) {
                builder.parameter(p.getName(), p.getValue());
            }
        }
    }

    private static void attachPublishedTestResultsIfPresent(JsonStringBuilder builder, Run run) {
        final AbstractTestResultAction action = run.getAction(AbstractTestResultAction.class);
        LOG.fine("AbstractTestResultAction = " + action);
        LOG.fine("TestResultAction = " + run.getAction(TestResultAction.class));
        if (action != null) {
            List<? extends TestResult> failedTests = action.getFailedTests();
            List<String> failedTestNames = new ArrayList<>();
            for (TestResult failedTest : failedTests) {
                failedTestNames.add(failedTest.getFullName());
            }
            builder.testResults(action.getTotalCount(), action.getSkipCount(), action.getFailCount(), failedTestNames);
        }

    }

    private static String getJenkinsRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }
}
