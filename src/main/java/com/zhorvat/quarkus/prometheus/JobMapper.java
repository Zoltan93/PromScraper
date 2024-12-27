package com.zhorvat.quarkus.prometheus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhorvat.quarkus.model.PrometheusJob;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JobMapper {

    private final ObjectMapper objectMapper;

    @Inject
    public JobMapper(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public PrometheusJob mapFromFile(String fileContent) {
        try {
            return objectMapper.readValue(fileContent, PrometheusJob.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("There was an exception, while mapping from file.", e);
        }
    }
}
