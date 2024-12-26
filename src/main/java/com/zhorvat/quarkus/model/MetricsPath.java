package com.zhorvat.quarkus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsPath {

    @JsonProperty("scrape_interval")
    private String scrape_interval;
}
