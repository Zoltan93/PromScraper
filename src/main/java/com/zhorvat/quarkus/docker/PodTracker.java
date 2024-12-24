package com.zhorvat.quarkus.docker;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class PodTracker {

    // TODO: Cache this
    private final Set<String> existingPods = Set.of();

    private final Set<String> newPods = Set.of();

    private final Set<String> oldPods = Set.of();

    public void track() {
        handleNewPods();
        handleDeletedPods();
    }

    private void handleNewPods() {

    }

    private void handleDeletedPods() {

    }
}
