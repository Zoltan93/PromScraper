package com.zhorvat.quarkus.docker;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Set;

@QuarkusTest
class ClientTests {

    private final Client dockerClient;

    @Inject
    ClientTests(
            Client dockerClient
    ) {
        this.dockerClient = dockerClient;
    }

    @Test
    void given_when_then() {
        Set<String> strings = dockerClient.listRunningContainerPublicPorts();
        System.out.println(strings.size());
    }
}
