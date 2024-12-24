package com.zhorvat.quarkus.prometheus;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class PodWatcher {

    private final JobManager jobManager;

    @Inject
    public PodWatcher(
            JobManager jobManager
    ) {
        this.jobManager = jobManager;
    }

    @Scheduled(initialDelayString = "10000",fixedRateString = "10000")
    public void watch() throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listPodForAllNamespaces().execute();
        DockerClient dockerClient = buildDockerClient(buildDockerClientConfig());
        dockerClient.listContainersCmd().exec().stream()
                .forEach(i -> System.out.println(i.getId()));
        Set<String> untrackedPods = new HashSet<>();
        for (V1Pod item : list.getItems()) {
            String namespace = item.getMetadata().getNamespace();
            if (jobManager.isJobMissingForPod(namespace)) {
                untrackedPods.add(namespace);
            }
        }
        untrackedPods.forEach(jobManager::manage);
    }

    private DockerClient buildDockerClient(DockerClientConfig config) {
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        return DockerClientImpl.getInstance(config, httpClient);
    }

    private DockerClientConfig buildDockerClientConfig() {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
    }
}
