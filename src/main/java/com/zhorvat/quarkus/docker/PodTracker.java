package com.zhorvat.quarkus.docker;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class PodTracker {

    private final Client dockerClient;

    private final Supplier<Set<Integer>> existingPodPorts = Suppliers.memoize(this::runningPodPorts);

    private final Set<Integer> newPodPorts = new HashSet<>();

    private final Set<Integer> oldPodPorts = new HashSet<>();

    @Inject
    public PodTracker(
            Client dockerClient
    ) {
        this.dockerClient = dockerClient;
    }

    public void track() {
//        Set<Integer> cachedPods = new HashSet<>(existingPodPorts.get());
//        Set<Integer> notRunningPods = new HashSet<>(cachedPods);
//        Set<Integer> runningPods = new HashSet<>(dockerClient.listRunningContainerPublicPorts());
//        Set<Integer> notCachedPods = new HashSet<>(runningPods);
//
//        notRunningPods.removeAll(runningPods);
//        notCachedPods.removeAll(cachedPods);
//
//        handleNewPods(notCachedPods);
//        handleDeletedPods(notRunningPods);
        existingPodPorts.get().clear();
        existingPodPorts.get().addAll(dockerClient.listRunningContainerPublicPorts());
    }

    private void handleNewPods(Set<Integer> runningPods) {
        newPodPorts.clear();
        newPodPorts.addAll(runningPods);
    }

    private void handleDeletedPods(Set<Integer> terminatedPods) {
        oldPodPorts.clear();
        oldPodPorts.addAll(terminatedPods);
    }

    private Set<Integer> runningPodPorts() {
        return dockerClient.listRunningContainerPublicPorts();
    }

    public Supplier<Set<Integer>> getExistingContainerPorts() {
        return existingPodPorts;
    }

    public Set<Integer> getNewPodPorts() {
        return newPodPorts;
    }

    public Set<Integer> getOldPodPorts() {
        return oldPodPorts;
    }
}
