package com.zhorvat.quarkus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrapeConfigs {

    private String job_name;
    private String metrics_path;
    private String scrape_interval;
    private List<StaticConfigs> static_configs;
}
