package org.jenkinsci.plugins.jpp;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageBuilderTest {

    @Test
    public void standardMessageCanBeCreated() {
        final String message = new MessageBuilder()
                .jenkinsUrl("http://mysite.com")
                .jobName("sample-task")
                .buildNumber(1)
                .buildString();
        assertThat(message).isEqualTo("{job_repository_url:http://mysite.com|job_name:sample-task|build_number:1}");
    }

}