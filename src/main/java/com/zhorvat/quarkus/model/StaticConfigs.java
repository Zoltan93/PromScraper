package com.zhorvat.quarkus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaticConfigs {

    private String[] targets;
    private Labels labels;
}
