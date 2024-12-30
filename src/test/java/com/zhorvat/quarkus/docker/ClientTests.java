package com.zhorvat.quarkus.docker;

import static com.google.inject.matcher.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@QuarkusTest
class ClientTests {

    private final Client client;
    private DockerClient dockerClient;

    @Inject
    ClientTests(
            Client client
    ) {
        this.client = client;
    }

    @Test
    void givenPrometheusContainerExists_whenRestartingContainer_thenStartedAtTimeIncreases() {
        dockerClient = client.buildDockerClient(client.buildDockerClientConfig());
        Instant containerBeforeRestart = getContainerUptime(getPrometheusContainerId());
        client.restartPrometheus();
        Awaitility.with()
                .pollDelay(4, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .await()
                .untilAsserted(() ->  assertThat(containerBeforeRestart, lessThan(getContainerUptime(getPrometheusContainerId()))));
    }

    private String getPrometheusContainerId() {
        Set<Container> runningContainers = client.getRunningContainers(dockerClient);
        return runningContainers.stream().map(container ->
                        Arrays.stream(container.getNames())
                                .filter(containerName -> containerName.contains("/prometheus"))
                                .findFirst())
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Prometheus container could not be found. It might have not been spun up during pre-integration-tests build phase."));
    }

    private Instant getContainerUptime(String containerId) {
        Object containerState = dockerClient.inspectContainerCmd(containerId).exec().getRawValues().get("State");
        Map<String, Object> map = new ObjectMapper()
                .convertValue(containerState, new TypeReference<>() {
        });
        return Instant.parse(map.get("StartedAt").toString());
    }
}
