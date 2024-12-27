package com.zhorvat.quarkus.file;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class YamlManagerTests {

    private final YamlManager yamlManager;

    private final String YAML_CONTENT = """
            |-
            scrape_configs:
                - job_name: 'backend'
                  metrics_path: '/actuator/prometheus'
                  scrape_interval: 3s
                  static_configs:
                    - targets: ['host.docker.internal:8081','host.docker.internal:8080']
                      labels:
                        application: 'app'""";

    @Inject
    public YamlManagerTests(
            YamlManager yamlManager
    ) {
        this.yamlManager = yamlManager;
    }

    @BeforeEach
    void writeYamlContent() {
        yamlManager.writeToPrometheusYaml(YAML_CONTENT);
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
