package com.zhorvat.quarkus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrometheusJob {

    @JsonProperty("scrape_configs")
    private ScrapeConfigs[] scrape_configs;

    //  scrape_configs:
    //      - job_name: 'backend'
    //        metrics_path: '/actuator/prometheus'
    //        scrape_interval: 3s
    //        static_configs:
    //          - targets: ['host.docker.internal:8081','host.docker.internal:8080']
    //            labels:
    //              application: 'app'
}
