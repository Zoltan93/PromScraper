package com.zhorvat.quarkus.docker;

import com.zhorvat.quarkus.file.FileManager;
import com.zhorvat.quarkus.prometheus.JobManager;
import io.kubernetes.client.openapi.ApiException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.springframework.scheduling.annotation.Scheduled;

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

    @Inject
    public PodWatcher(
            JobManager jobManager,
            PodTracker podTracker,
            FileManager fileManager,
            Client dockerClient
    ) {
        this.jobManager = jobManager;
        this.podTracker = podTracker;
        this.fileManager = fileManager;
        this.dockerClient = dockerClient;
    }

    @PostConstruct
    public void syncJobAndPods() {
        Set<String> runningPodPorts = podTracker.getExistingPodPorts().get()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());
        String fileContent = fileManager.readFromFile();
        Set<String> ports = new HashSet<>();
        runningPodPorts.stream()
                .filter(port -> !fileContent.contains(port))
                .forEach(ports::add);
        if (!ports.isEmpty()) {
            jobManager.manage(ports);
            dockerClient.restartPrometheus();
        }
    }

    @Scheduled(initialDelayString = "10000", fixedRateString = "10000")
    public void watch() throws IOException, ApiException {
//        ApiClient client = Config.defaultClient();
//        Configuration.setDefaultApiClient(client);
//
//        CoreV1Api api = new CoreV1Api();
//        V1PodList list = api.listPodForAllNamespaces().execute();
//
//        Set<String> untrackedPods = new HashSet<>();
//        for (V1Pod item : list.getItems()) {
//            String namespace = item.getMetadata().getNamespace();
//            if (jobManager.isJobMissingForPod(namespace)) {
//                untrackedPods.add(namespace);
//            }
//        }
//        untrackedPods.forEach(jobManager::manage);
        podTracker.track();
        Set<String> runningPodPorts = podTracker.getExistingPodPorts().get()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());
        String fileContent = fileManager.readFromFile();
        Set<String> ports = new HashSet<>();
        runningPodPorts.stream()
                .filter(port -> !fileContent.contains(port))
                .forEach(ports::add);
        if (!ports.isEmpty()) {
            jobManager.manage(ports);
            dockerClient.restartPrometheus();
        }
//        podTracker.getNewPods().forEach(jobManager::manage);
    }


}
