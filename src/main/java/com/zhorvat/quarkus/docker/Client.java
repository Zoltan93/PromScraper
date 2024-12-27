package com.zhorvat.quarkus.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class Client {

    public void restartPrometheus() {
        try (DockerClient dockerClient = buildDockerClient(buildDockerClientConfig())) {
            Set<Container> containers = getRunningContainers(dockerClient);
            containers.forEach(container ->
                    Arrays.stream(container.getNames())
                            .filter(containerName -> containerName.contains("/prometheus"))
                            .findFirst()
                            .ifPresent(
                                    operation -> dockerClient.restartContainerCmd(container.getId()).exec()
                            )
            );
        } catch (IOException e) {
            throw new RuntimeException("There was an issue with the docker client", e);
        }
    }

    public Set<String> listRunningContainerPublicPorts() {
        try (DockerClient dockerClient = buildDockerClient(buildDockerClientConfig())) {
            Set<Container> containers = getRunningContainers(dockerClient);
            return containers.stream()
                    .flatMap(container ->
                            Arrays.stream(container.getNames())
                                    .filter(containerName -> !"/prometheus".equalsIgnoreCase(containerName))
                                    .map(nonPrometheusContainer -> container)
                    )
                    .map(container ->
                            Arrays.stream(container.getPorts())
                                    .filter(port -> Objects.nonNull(port.getPublicPort()))
                                    .map(ContainerPort::getPublicPort)
                                    .collect(Collectors.toSet())
                    )
                    .flatMap(Set::stream)
                    .map(String::valueOf)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("There was an issue with the docker client", e);
        }
    }

    private Set<Container> getRunningContainers(DockerClient dockerClient) {
        return new HashSet<>(
                dockerClient
                        .listContainersCmd()
                        .exec()
        );
    }

    private DockerClient buildDockerClient(DockerClientConfig config) {
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();
        return DockerClientImpl.getInstance(config, httpClient);
    }

    private DockerClientConfig buildDockerClientConfig() {
        return DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
    }
}
