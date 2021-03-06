package org.jenkinsci.plugins.bpp;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonStringBuilderTest {

    @Test
    public void standardMessageCanBeCreated() {
        final String message = new JsonStringBuilder()
                .jenkinsUrl("http://mysite.com")
                .jobName("sample-task")
                .buildNumber(1)
                .buildString();
        assertThat(message).isEqualTo("{\"jenkins_url\":\"http://mysite.com\",\"job_name\":\"sample-task\",\"build_number\":1}");
    }
}