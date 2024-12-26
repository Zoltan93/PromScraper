package com.zhorvat.quarkus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrapeConfigs {

    private JobName job_name;
    private MetricsPath metrics_path;
    private StaticConfigs[] static_configs;
    private ScrapeInterval scrape_interval;
}
