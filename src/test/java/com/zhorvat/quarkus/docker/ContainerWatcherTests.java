package com.zhorvat.quarkus.docker;

import static com.zhorvat.quarkus.prometheus.JobManager.jobTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import com.zhorvat.quarkus.file.YamlManager;
import com.zhorvat.quarkus.prometheus.JobMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@QuarkusTest
class ContainerWatcherTests {

    private final YamlManager yamlManager;
    private final Client dockerClient;
    private final JobMapper jobMapper;
    private final ContainerWatcher containerWatcher;

    @Inject
    public ContainerWatcherTests(
            YamlManager yamlManager,
            Client dockerClient,
            JobMapper jobMapper,
            ContainerWatcher containerWatcher
    ) {
        this.yamlManager = yamlManager;
        this.dockerClient = dockerClient;
        this.jobMapper = jobMapper;
        this.containerWatcher = containerWatcher;
    }

    @BeforeEach
    void clearPrometheusTargets() {
        yamlManager.writeToPrometheusYaml(jobTemplate(Collections.emptySet()));
    }

    @Test
    void given_when_then() {
        waitForFileCondition(not(containsString("9091")));
        containerWatcher.watch();
        waitForFileCondition(containsString("9091"));
    }

    private void waitForFileCondition(Matcher<String> condition) {
        Awaitility.with()
                .pollDelay(4, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .await()
                .untilAsserted(() -> assertThat(yamlManager.read(), condition));
    }
}
