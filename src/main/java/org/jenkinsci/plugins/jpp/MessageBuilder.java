package org.jenkinsci.plugins.jpp;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public final class MessageBuilder {

    private final static String JENKINS_URL = "jenkins_url";
    private final static String BUILD_JOB_NAME = "job_name";
    private final static String BUILD_NUMBER = "build_number";

    private final Map<String, Object> map = new HashMap<>();

    public MessageBuilder jenkinsUrl(String url) {
        map.put(JENKINS_URL, url);
        return this;
    }

    public MessageBuilder jobName(String jobName) {
        map.put(BUILD_JOB_NAME, jobName);
        return this;
    }

    public MessageBuilder buildNumber(int buildNumber) {
        map.put(BUILD_NUMBER, buildNumber);
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
            writer.endObject();
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected JSONException", e);
        }

        return swriter.toString();
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

}
