package com.zhorvat.quarkus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;

public class MapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper(new YAMLFactory());
    }
}
