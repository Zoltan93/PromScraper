package com.zhorvat.quarkus.prometheus;

import com.zhorvat.quarkus.file.FileManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

@ApplicationScoped
public class JobManager {

    private final String fileName;

    private final FileManager fileManager;

    @Inject
    public JobManager(
            @ConfigProperty(name = "prometheusFile.name") String fileName,
            FileManager fileManager
    ) {
        this.fileName = fileName;
        this.fileManager = fileManager;
    }

    public boolean isJobMissingForPod(String pod) {
        return !fileManager.isPodPresent(pod);
    }

    public void manage(Set<String> ports) {
        fileManager.writeToFile(fileName, jobTemplate(ports));
    }

    public static String jobTemplate(Set<String> ports) {
        System.out.println("Should be here");
        Set<String> socket = ports.stream().map(port -> String.format("'0.0.0.0:%s'", port)).collect(Collectors.toSet());
        return String.format("""
                scrape_configs:
                    - job_name: 'backend'
                      metrics_path: '/actuator/prometheus'
                      scrape_interval: 3s
                      static_configs:
                        - targets: [%s]
                          labels:
                            application: 'app'""", String.join(",", socket));
    }
}
