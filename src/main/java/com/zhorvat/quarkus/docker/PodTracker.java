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

    private final Supplier<Set<String>> existingPods = Suppliers.memoize(this::runningPods);

    private final Set<String> newPods = new HashSet<>();

    private final Set<String> oldPods = new HashSet<>();

    @Inject
    public PodTracker(
            Client dockerClient
    ) {
        this.dockerClient = dockerClient;
    }

    public void track() {
        Set<String> cachedPods = new HashSet<>(existingPods.get());
        Set<String> notRunningPods = new HashSet<>(cachedPods);
        Set<String> runningPods = new HashSet<>(dockerClient.listRunningPodIds());
        Set<String> notCachedPods = new HashSet<>(runningPods);

        notRunningPods.removeAll(runningPods);
        notCachedPods.removeAll(cachedPods);

        handleNewPods(notCachedPods);
        handleDeletedPods(notRunningPods);
    }

    private void handleNewPods(Set<String> runningPods) {
        newPods.clear();
        newPods.addAll(runningPods);
    }

    private void handleDeletedPods(Set<String> terminatedPods) {
        oldPods.clear();
        oldPods.addAll(terminatedPods);
    }

    private Set<String> runningPods() {
        return dockerClient.listRunningPodIds();
    }

    public Supplier<Set<String>> getExistingPods() {
        return existingPods;
    }

    public Set<String> getNewPods() {
        return newPods;
    }

    public Set<String> getOldPods() {
        return oldPods;
    }
}
