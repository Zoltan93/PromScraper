package com.zhorvat.quarkus.docker;

import static com.zhorvat.quarkus.prometheus.JobManager.jobTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.zhorvat.quarkus.file.YamlManager;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@QuarkusTest
class ContainerWatcherTests {

    private final YamlManager yamlManager;
    private final Client client;
    private DockerClient dockerClient;
    private final ContainerWatcher containerWatcher;

    @Inject
    public ContainerWatcherTests(
            YamlManager yamlManager,
            Client client,
            ContainerWatcher containerWatcher
    ) {
        this.yamlManager = yamlManager;
        this.client = client;
        this.containerWatcher = containerWatcher;
    }

    @BeforeEach
    void clearPrometheusTargets() {
        yamlManager.writeToPrometheusYaml(jobTemplate(Collections.emptySet()));

        dockerClient = client.buildDockerClient(client.buildDockerClientConfig());
        Supplier<Set<Container>> containersInExitedState = () -> dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container ->
                        "exited".equalsIgnoreCase(container.getState())
                )
                .collect(Collectors.toSet());
        containersInExitedState.get().forEach(container -> startContainer(dockerClient, container));
        waitForCondition(containersInExitedState.get().isEmpty());
    }

    @Test
    void givenEmptyPrometheusTargetList_whenContainerWatcherIsCalled_thenAdequateTargetIsPopulated() {
        waitForFileCondition(not(containsString("9091")));
        containerWatcher.watch();
        waitForFileCondition(containsString("9091"));
    }

    @Test
    void givenNonEmptyPrometheusTargetList_whenContainerIsStoppedAndContainerWatcherIsCalled_thenAdequateTargetIsPopulated() {
        waitForFileCondition(not(containsString("9091")));
        containerWatcher.watch();
        waitForFileCondition(containsString("9091"));

        Set<Container> runningContainers = client.getRunningContainers(dockerClient);
        stopContainer(dockerClient, runningContainers, "/rabbitmq");
        Awaitility.with()
                .pollDelay(2, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .await()
                .until(() -> client.getRunningContainers(dockerClient).size() == 1);
        containerWatcher.watch();
        waitForFileCondition(not(containsString("9091")));
    }

    @Test
    void givenNoContainerRunning_whenContainerWatcherIsCalled_thenAdequateTargetIsPopulated() {
        Set<Container> runningContainers = client.getRunningContainers(dockerClient);
        stopContainer(dockerClient, runningContainers, "/rabbitmq");
        Awaitility.with()
                .pollDelay(2, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .await()
                .until(() -> client.getRunningContainers(dockerClient).size() == 1);
//        waitForCondition(client.getRunningContainers(dockerClient).size() == 1);
        containerWatcher.watch();
        waitForFileCondition(not(containsString("9091")));
    }


    public void stopContainer(
            DockerClient dockerClient,
            Set<Container> containers,
            String containerToStop
    ) {
        containers.forEach(container ->
                Arrays.stream(container.getNames())
                        .filter(containerToStop::equalsIgnoreCase)
                        .findFirst()
                        .ifPresent(operation ->
                                dockerClient.stopContainerCmd(container.getId()).exec()
                        )
        );
    }

    public void startContainer(
            DockerClient dockerClient,
            Container containerToStart
    ) {
        dockerClient.startContainerCmd(containerToStart.getId()).exec();
    }

    private void waitForFileCondition(Matcher<String> condition) {
        Awaitility.with()
                .pollDelay(4, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .await()
                .untilAsserted(() -> assertThat(yamlManager.read(), condition));
    }

    private void waitForCondition(Boolean condition) {
        Awaitility.with()
                .pollDelay(2, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .await()
                .until(() -> condition);
    }
}
