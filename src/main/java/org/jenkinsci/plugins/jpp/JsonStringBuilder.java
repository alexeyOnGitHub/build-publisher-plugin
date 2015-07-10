package org.jenkinsci.plugins.jpp;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class does not know anything about Jenkins. It builds a valid Json string using standard Java types.
 */
final class JsonStringBuilder {

    private static final String JENKINS_URL = "jenkins_url";
    private static final String BUILD_JOB_NAME = "job_name";
    private static final String BUILD_NUMBER = "build_number";
    private static final String RESULT = "result";
    private static final String BUILD_ID = "id";
    private static final String PARAMETERS = "parameters";
    private static final String TEST_RESULTS = "test_results";
    private static final String TOTAL_COUNT = "total_count";
    private static final String SKIP_COUNT = "skip_count";
    private static final String FAIL_COUNT = "fail_count";
    private static final String FAILED_TESTS = "failed_tests";

    private final Map<String, Object> map = new HashMap<>();
    private final Map<String, Object> parametersMap = new HashMap<>();

    public JsonStringBuilder jenkinsUrl(String url) {
        map.put(JENKINS_URL, url);
        return this;
    }

    public JsonStringBuilder jobName(String jobName) {
        map.put(BUILD_JOB_NAME, jobName);
        return this;
    }

    public JsonStringBuilder buildNumber(int buildNumber) {
        map.put(BUILD_NUMBER, buildNumber);
        return this;
    }

    public JsonStringBuilder result(String result) {
        map.put(RESULT, result);
        return this;
    }

    public String buildString() {
        final StringWriter swriter = new StringWriter();
        final JSONWriter writer = new JSONWriter(swriter);
        try {
            writer.object();
            addIfNotNull(writer, JENKINS_URL, (String) map.get(JENKINS_URL));
            addIfNotNull(writer, BUILD_JOB_NAME, (String) map.get(BUILD_JOB_NAME));
            addIfNotNull(writer, BUILD_NUMBER, (Integer) map.get(BUILD_NUMBER));
            addIfNotNull(writer, RESULT, (String) map.get(RESULT));
            addIfNotNull(writer, BUILD_ID, (String) map.get(BUILD_ID));

            addParametersIfPresent(writer);
            addTestResultsIfPresent(writer);
            writer.endObject();
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected JSONException", e);
        }

        return swriter.toString();
    }

    private void addParametersIfPresent(JSONWriter writer) {
        if (!parametersMap.isEmpty()) {
            addParameters(writer);
        }
    }

    private void addParameters(JSONWriter writer) {
        writer.key(PARAMETERS).object();
        for (Map.Entry<String, Object> entry : parametersMap.entrySet()) {
            writer.key(entry.getKey().toString()).value(entry.getValue());
        }
        writer.endObject();
    }

    private void addTestResultsIfPresent(JSONWriter writer) {
        if (map.containsKey(TOTAL_COUNT)) {
            addTestResults(writer);
        }
    }

    private void addTestResults(JSONWriter writer) {
        writer.key(TEST_RESULTS).object();

        writer.key(TOTAL_COUNT).value(map.get(TOTAL_COUNT));
        writer.key(SKIP_COUNT).value(map.get(SKIP_COUNT));
        writer.key(FAIL_COUNT).value(map.get(FAIL_COUNT));

        writer.key(FAILED_TESTS).array();
        final List<String> failedTestNames = (List<String>) map.get(FAILED_TESTS);
        for (String entry : failedTestNames) {
            writer.value(entry);
        }
        writer.endArray();

        writer.endObject();
    }

    private static void addIfNotNull(JSONWriter writer, String field, String value) throws JSONException {
        if (value == null)
            return;
        writer.key(field);
        writer.value(value);
    }

    // TODO delete this. make it Object!
    private static void addIfNotNull(JSONWriter writer, String field, Integer value) throws JSONException {
        if (value == null)
            return;
        writer.key(field);
        writer.value(value);
    }

    public JsonStringBuilder parameter(String name, Object value) {
        parametersMap.put(name, value);
        return this;
    }

    /**
     * Unique Build ID
     */
    public JsonStringBuilder id(String id) {
        map.put(BUILD_ID, id);
        return this;
    }

    public JsonStringBuilder testResults(int totalCount, int skipCount, int failCount, List<String> failedTestNames) {
        map.put(TOTAL_COUNT, totalCount);
        map.put(SKIP_COUNT, skipCount);
        map.put(FAIL_COUNT, failCount);
        map.put(FAILED_TESTS, failedTestNames);
        return this;
    }
}
