package com.zhorvat.quarkus.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zhorvat.quarkus.file.FileManager;
import com.zhorvat.quarkus.model.PrometheusJob;
import com.zhorvat.quarkus.prometheus.JobManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class PodWatcher {

    private final JobManager jobManager;
    private final PodTracker podTracker;
    private final FileManager fileManager;
    private final Client dockerClient;
    private final ObjectMapper objectMapper;
    private static boolean isEmptyContainerCaseHandled = false;

    @Inject
    public PodWatcher(
            JobManager jobManager,
            PodTracker podTracker,
            FileManager fileManager,
            Client dockerClient,
            ObjectMapper objectMapper
    ) {
        this.jobManager = jobManager;
        this.podTracker = podTracker;
        this.fileManager = fileManager;
        this.dockerClient = dockerClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(initialDelayString = "10000", fixedRateString = "10000")
    public void watch() {
        podTracker.track();
        Set<String> runningContainerPorts = podTracker.getExistingContainerPorts().get()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());
        String fileContent = fileManager.readFromFile();
        Set<String> ports = new HashSet<>(runningContainerPorts);
        Set<String> missingPorts = ports.stream()
                .filter(port -> !fileContent.contains(port))
                .collect(Collectors.toSet());
        Set<String> portsStillInFile = ports.stream()
                .filter(fileContent::contains)
                .collect(Collectors.toSet());
        portsStillInFile.removeAll(runningContainerPorts);
        if (!missingPorts.isEmpty()) {
            jobManager.manage(ports);
            dockerClient.restartPrometheus();
            isEmptyContainerCaseHandled = false;
        } else if (!portsStillInFile.isEmpty()) {
            jobManager.manage(ports);
            dockerClient.restartPrometheus();
            isEmptyContainerCaseHandled = false;
            // Handle case, when there are no containers running
        } else if (runningContainerPorts.isEmpty() && !isEmptyContainerCaseHandled) {
            jobManager.manage(ports);
            dockerClient.restartPrometheus();
            isEmptyContainerCaseHandled = true;
        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File("src/main/resources/prometheus.yml");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        PrometheusJob config = objectMapper.readValue(file,PrometheusJob.class);
        System.out.println("Application config info " + config.toString());
    }
}
