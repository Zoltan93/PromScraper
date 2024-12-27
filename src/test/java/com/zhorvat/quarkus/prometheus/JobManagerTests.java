package com.zhorvat.quarkus.prometheus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

@QuarkusTest
class JobManagerTests {

    @Test
    void givenSetOfPorts_whenCreatingJobTemplate_thenPortsAreIncluded() {
        Set<String> ports = Set.of("1234", "5678");
        String jobTemplate = JobManager.jobTemplate(ports);

        ports.forEach(port ->
                assertThat(jobTemplate, containsString(port))
        );
    }

    @Test
    void givenEmptySetPorts_whenCreatingJobTemplate_thenPortsAreNotIncluded() {
        String jobTemplate = JobManager.jobTemplate(Collections.emptySet());

        assertThat(jobTemplate, not(containsString("host.docker.internal")));
    }
}
