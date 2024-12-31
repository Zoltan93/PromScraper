package com.zhorvat.quarkus.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class ContainerTestUtils {

    private static final DockerClient dockerClient;
    private static final Client client;

    static {
        client = new Client();
        dockerClient = client.buildDockerClient(client.buildDockerClientConfig());
    }

    static Supplier<Set<Container>> getRunningContainers() {
        return () -> client.getRunningContainers(dockerClient);
    }

    static void stopContainer(
            Set<Container> containers,
            String containerToStop
    ) {
        containers.forEach(container ->
                Arrays.stream(container.getNames())
                        .filter(containerToStop::equalsIgnoreCase)
                        .findFirst()
                        .ifPresent(operation ->
                                dockerClient.stopContainerCmd(container.getId()).exec()
                        )
        );
    }

    static void startContainer(
            Container containerToStart
    ) {
        dockerClient.startContainerCmd(containerToStart.getId()).exec();
    }

    static Set<Container> getContainersInState(DockerContainerState state) {
        return dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container ->
                        state.getState().equalsIgnoreCase(container.getState())
                )
                .collect(Collectors.toSet());
    }

    @Getter
    public enum DockerContainerState {
        EXITED("exited");

        private final String state;

        DockerContainerState(String state) {
            this.state = state;
        }
    }
}
