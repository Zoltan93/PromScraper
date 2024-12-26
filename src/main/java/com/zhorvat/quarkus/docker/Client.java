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
            Set<Container> containers = new HashSet<>(
                    dockerClient
                            .listContainersCmd()
                            .exec()
            );
            for (Container container : containers) {
                Set<String> containerNames = Set.of(container.getNames());
                for (String containerName : containerNames) {
                    if ("/prometheus".equalsIgnoreCase(containerName)) {
                        dockerClient.restartContainerCmd(container.getId()).exec();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There was an issue with the docker client", e);
        }
    }

    public Set<Integer> listRunningContainerPublicPorts() {
        try (DockerClient dockerClient = buildDockerClient(buildDockerClientConfig())) {
            Set<Container> containers = new HashSet<>(
                    dockerClient
                            .listContainersCmd()
                            .exec()
            );
            Set<Integer> publicPorts = new HashSet<>();
            for (Container container : containers) {
                Set<String> containerNames = Set.of(container.getNames());
                for (String containerName : containerNames) {
                    if ("/prometheus".equalsIgnoreCase(containerName)) {
                        continue;
                    }
                    Arrays.stream(container.getPorts())
                            .filter(port -> Objects.nonNull(port.getPublicPort()))
                            .map(ContainerPort::getPublicPort)
                            .forEach(publicPorts::add);
                }
            }
            return publicPorts;
//            return dockerClient
//                    .listContainersCmd()
//                    .exec()
//                    .stream()
//                    .map(pod -> {
//                        Set<ContainerPort> collect = Arrays.stream(pod.getPorts()).collect(Collectors.toSet());
//                        return collect.stream().map(ContainerPort::getPublicPort).collect(Collectors.toSet());
//                    })
//                    .flatMap(Set::stream)
//                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("There was an issue with the docker client", e);
        }
    }

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
                .build();
        return DockerClientImpl.getInstance(config, httpClient);
    }

    private DockerClientConfig buildDockerClientConfig() {
        return DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
    }
}
