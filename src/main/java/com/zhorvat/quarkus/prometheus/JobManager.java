package com.zhorvat.quarkus.prometheus;

import com.zhorvat.quarkus.file.FileManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;
import java.util.stream.Collectors;

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

    public void manage(Set<String> ports) {
        fileManager.writeToFile(fileName, jobTemplate(ports));
    }

    public static String jobTemplate(Set<String> ports) {
        Set<String> socket = ports.stream().map(port -> String.format("'host.docker.internal:%s'", port)).collect(Collectors.toSet());
        if (ports.isEmpty()) {
            return """
                scrape_configs:
                    - job_name: 'backend'
                      metrics_path: '/actuator/prometheus'
                      scrape_interval: 3s
                      static_configs:
                        - targets: []
                          labels:
                            application: 'app'""";
        }
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
