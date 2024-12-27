package com.zhorvat.quarkus.prometheus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhorvat.quarkus.model.PrometheusJob;
import com.zhorvat.quarkus.model.ScrapeConfigs;
import com.zhorvat.quarkus.model.StaticConfigs;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

@QuarkusTest
class JobMapperTests {

    private final String FILE_CONTENT = """
            scrape_configs:
                - job_name: 'backend'
                  metrics_path: '/actuator/prometheus'
                  scrape_interval: 3s
                  static_configs:
                    - targets: ['host.docker.internal:8081']
                      labels:
                        application: 'app'""";

    private final JobMapper jobMapper;

    @Inject
    public JobMapperTests(
            JobMapper jobMapper
    ) {
        this.jobMapper = jobMapper;
    }

    @Test
    void givenObjectAsString_whenMappingToObject_thenCorrectObjectIsReturned() {
        PrometheusJob prometheusJob = jobMapper.mapFromFile(FILE_CONTENT);
        List<ScrapeConfigs> scrapeConfigs = prometheusJob.getScrape_configs();

        assertNotNull(scrapeConfigs);
        assertThat(scrapeConfigs, is(not(Collections.emptyList())));

        for (ScrapeConfigs scrapeConfig : scrapeConfigs) {
            StaticConfigs staticConfigs = scrapeConfig.getStatic_configs().get(0);
            assertEquals(scrapeConfig.getJob_name(), "backend");
            assertEquals(scrapeConfig.getMetrics_path(), "/actuator/prometheus");
            assertEquals(scrapeConfig.getScrape_interval(), "3s");
            assertThat(staticConfigs.getTargets().get(0), containsString("host.docker.internal:8081"));
            assertThat(staticConfigs.getLabels().getApplication(), containsString("app"));
        }
    }

    @Test
    void givenInvalidObjectAsString_whenMappingToObject_thenExceptionIsThrown() {
        assertThrows(RuntimeException.class, () -> jobMapper.mapFromFile("dummy-content"));
    }
}
