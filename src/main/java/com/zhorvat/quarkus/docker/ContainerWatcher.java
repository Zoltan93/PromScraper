package com.zhorvat.quarkus.docker;

import static com.zhorvat.quarkus.prometheus.JobManager.jobTemplate;

import com.zhorvat.quarkus.file.YamlManager;
import com.zhorvat.quarkus.model.PrometheusJob;
import com.zhorvat.quarkus.prometheus.JobMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContainerWatcher {

    private static boolean isEmptyContainerCaseHandled = false;

    private final YamlManager yamlManager;
    private final Client dockerClient;
    private final JobMapper jobMapper;

    @Inject
    public ContainerWatcher(
            YamlManager yamlManager,
            Client dockerClient,
            JobMapper jobMapper
    ) {
        this.yamlManager = yamlManager;
        this.dockerClient = dockerClient;
        this.jobMapper = jobMapper;
    }

    @Scheduled(initialDelayString = "5000", fixedRateString = "5000")
    public void watch() {
        Set<String> runningContainerPorts = dockerClient.listRunningContainerPublicPorts();
        Set<String> targets = getPrometheusJobTargets();
        if (runningContainerPorts.size() != targets.size()) {
            handleContainerChanges(runningContainerPorts);
            isEmptyContainerCaseHandled = false;
        } else if (runningContainerPorts.isEmpty() && !isEmptyContainerCaseHandled) {
            handleContainerChanges(runningContainerPorts);
            isEmptyContainerCaseHandled = true;
        }
    }

    private Set<String> getPrometheusJobTargets() {
        PrometheusJob config = jobMapper.mapFromFile(yamlManager.read());
        return Arrays.stream(config.getScrape_configs())
                .map(scrape -> Arrays.stream(scrape.getStatic_configs())
                        .map(staticConfig -> Arrays.stream(staticConfig.getTargets())
                                .collect(Collectors.toSet()))
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet()))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private void handleContainerChanges(Set<String> runningContainerPorts) {
        yamlManager.writeToPrometheusYaml(jobTemplate(runningContainerPorts));
        dockerClient.restartPrometheus();
    }
}
