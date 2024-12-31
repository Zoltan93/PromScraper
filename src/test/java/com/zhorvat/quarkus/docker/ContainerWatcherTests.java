package com.zhorvat.quarkus.docker;

import static com.zhorvat.quarkus.docker.ContainerTestUtils.DockerContainerState.EXITED;
import static com.zhorvat.quarkus.docker.ContainerTestUtils.getContainersInState;
import static com.zhorvat.quarkus.docker.ContainerTestUtils.getRunningContainers;
import static com.zhorvat.quarkus.docker.ContainerTestUtils.stopContainer;
import static com.zhorvat.quarkus.prometheus.JobManager.jobTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import com.github.dockerjava.api.model.Container;
import com.zhorvat.quarkus.file.YamlManager;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@QuarkusTest
class ContainerWatcherTests extends ContainerTestUtils {

    private final YamlManager yamlManager;
    private final ContainerWatcher containerWatcher;

    @Inject
    public ContainerWatcherTests(
            YamlManager yamlManager,
            ContainerWatcher containerWatcher
    ) {
        this.yamlManager = yamlManager;
        this.containerWatcher = containerWatcher;
    }

    @BeforeEach
    void clearPrometheusTargets() {
        yamlManager.writeToPrometheusYaml(jobTemplate(Collections.emptySet()));
        Supplier<Set<Container>> containersInExitedState = () -> getContainersInState(EXITED);
        containersInExitedState.get().forEach(ContainerTestUtils::startContainer);
        waitForCondition(() -> containersInExitedState.get().isEmpty());
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
        stopContainer(getRunningContainers().get(), "/rabbitmq");
        waitForCondition(() -> getRunningContainers().get().size() == 1);
        containerWatcher.watch();
        waitForFileCondition(not(containsString("9091")));
    }

    @Test
    void givenNoContainerRunning_whenContainerWatcherIsCalled_thenAdequateTargetIsPopulated() {
        stopContainer(getRunningContainers().get(), "/rabbitmq");
        waitForCondition(() -> getRunningContainers().get().size() == 1);
        containerWatcher.watch();
        waitForFileCondition(not(containsString("9091")));
    }


    private void waitForFileCondition(Matcher<String> condition) {
        Awaitility.with()
                .pollDelay(4, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .await()
                .untilAsserted(() -> assertThat(yamlManager.read(), condition));
    }

    private void waitForCondition(Callable<Boolean> condition) {
        Awaitility.with()
                .pollDelay(2, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .await()
                .until(condition);
    }
}
