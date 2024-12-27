package com.zhorvat.quarkus.docker;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;

@ApplicationScoped
public class PodTracker {

    private final Client dockerClient;

    @Inject
    public PodTracker(
            Client dockerClient
    ) {
        this.dockerClient = dockerClient;
    }

    public Set<Integer> getExistingContainerPorts() {
        return dockerClient.listRunningContainerPublicPorts();
    }
}
