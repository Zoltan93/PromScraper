package com.zhorvat.quarkus.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class Client {

    public Set<String> listRunningPodIds() {
        try (DockerClient dockerClient = buildDockerClient(buildDockerClientConfig())) {
            return dockerClient
                    .listContainersCmd()
                    .exec()
                    .stream()
                    .map(Container::getId)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("There was an issue with the docker client", e);
        }
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
        return DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
    }
}
