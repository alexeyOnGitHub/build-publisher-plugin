package org.jenkinsci.plugins.jpp;

import java.util.HashMap;
import java.util.Map;

public final class MessageBuilder {

    private final static String JENKINS_URL = "job_repository_url";
    private final static String BUILD_JOB_NAME = "job_name";
    private final static String BUILD_NUMBER = "build_number";

    private final Map<String, String> map = new HashMap<>();

    public MessageBuilder jenkinsUrl(String url) {
        map.put(JENKINS_URL, url);
        return this;
    }

    public MessageBuilder jobName(String jobName) {
        map.put(BUILD_JOB_NAME, jobName);
        return this;
    }

    public MessageBuilder buildNumber(long buildNumber) {
        map.put(BUILD_NUMBER, Long.toString(buildNumber));
        return this;
    }

    public String buildString() {
        return String.format("{job_repository_url:%s|job_name:%s|build_number:%s}",
                map.get(JENKINS_URL), map.get(BUILD_JOB_NAME), map.get(BUILD_NUMBER));
    }
}
