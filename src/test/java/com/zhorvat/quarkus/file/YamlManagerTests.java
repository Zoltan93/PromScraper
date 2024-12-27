package com.zhorvat.quarkus.file;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@QuarkusTest
class YamlManagerTests {

    private final String FILE_CONTENT = """
            |-
            scrape_configs:
                - job_name: 'backend'
                  metrics_path: '/actuator/prometheus'
                  scrape_interval: 3s
                  static_configs:
                    - targets: ['host.docker.internal:8081','host.docker.internal:8080']
                      labels:
                        application: 'app'""";

    private final YamlManager yamlManager;

    @Inject
    public YamlManagerTests(
            YamlManager yamlManager
    ) {
        this.yamlManager = yamlManager;
    }

    @BeforeEach
    void writeYamlContent() {
        yamlManager.writeToPrometheusYaml(FILE_CONTENT);
    }

    @Test
    void givenContentExists_whenWritingToPrometheusYaml_thenContentIsPresentInYaml() {
        assertThat(yamlManager.read(), containsString("host.docker.internal:8080"));
    }

    @Test
    void givenUnnecessaryCharacterInYamlFile_whenRemovingDanglingCharacter_thenCharacterIsNotPresentInYaml() throws IOException {
        yamlManager.removeDanglingCharacter();

        assertThat(yamlManager.read(), not(containsString("|-")));
    }
}
