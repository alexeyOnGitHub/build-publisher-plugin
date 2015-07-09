package org.jenkinsci.plugins.jpp;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public final class JsonStringBuilder {

    private final static String JENKINS_URL = "jenkins_url";
    private final static String BUILD_JOB_NAME = "job_name";
    private final static String BUILD_NUMBER = "build_number";
    private final static String RESULT = "result";

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

            addParametersIfPresent(writer);
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
        writer.key("parameters").object();
        for (Map.Entry<String, Object> entry : parametersMap.entrySet()) {
            writer.key(entry.getKey().toString()).value(entry.getValue());
        }
        writer.endObject();
    }

    private static void addIfNotNull(JSONWriter writer, String field, String value) throws JSONException {
        if (value == null)
            return;
        writer.key(field);
        writer.value(value);
    }


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
}
