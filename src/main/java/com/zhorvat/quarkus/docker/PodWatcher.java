package com.zhorvat.quarkus.docker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhorvat.quarkus.file.FileManager;
import com.zhorvat.quarkus.model.PrometheusJob;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zhorvat.quarkus.prometheus.JobManager.jobTemplate;

@ApplicationScoped
public class PodWatcher {

    private final FileManager fileManager;
    private final Client dockerClient;
    private final ObjectMapper objectMapper;
    private static boolean isEmptyContainerCaseHandled = false;

    @Inject
    public PodWatcher(
            FileManager fileManager,
            Client dockerClient,
            ObjectMapper objectMapper
    ) {
        this.fileManager = fileManager;
        this.dockerClient = dockerClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(initialDelayString = "10000", fixedRateString = "10000")
    public void watch() throws JsonProcessingException {
        Set<String> runningContainerPorts = dockerClient.listRunningContainerPublicPorts()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());
        Set<String> ports = new HashSet<>(runningContainerPorts);
        PrometheusJob config = objectMapper.readValue(fileManager.readFromPrometheusYaml(), PrometheusJob.class);
        Set<String> targets = Arrays.stream(config.getScrape_configs())
                .map(scrape -> Arrays.stream(scrape.getStatic_configs())
                        .map(staticConfig -> Arrays.stream(staticConfig.getTargets())
                                .collect(Collectors.toSet()))
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet()))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        if (runningContainerPorts.size() != targets.size()) {
            fileManager.writeToPrometheusYaml(jobTemplate(ports));
            dockerClient.restartPrometheus();
            isEmptyContainerCaseHandled = false;
        } else if (runningContainerPorts.isEmpty() && !isEmptyContainerCaseHandled) {
            fileManager.writeToPrometheusYaml(jobTemplate(ports));
            dockerClient.restartPrometheus();
            isEmptyContainerCaseHandled = true;
        }
    }
}
