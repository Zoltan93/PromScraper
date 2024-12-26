package com.zhorvat.quarkus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.inject.Singleton;

public class MapperConfig {

    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper(new YAMLFactory());
    }
}
