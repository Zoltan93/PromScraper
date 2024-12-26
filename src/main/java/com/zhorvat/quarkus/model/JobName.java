package com.zhorvat.quarkus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobName {

    @JsonProperty("metrics_path")
    private String metrics_path;
}
