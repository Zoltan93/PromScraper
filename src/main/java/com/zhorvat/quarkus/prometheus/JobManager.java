package com.zhorvat.quarkus.prometheus;

import com.zhorvat.quarkus.file.FileManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class JobManager {


    private final FileManager fileManager;

    @Inject
    public JobManager(
            FileManager fileManager
    ) {
        this.fileManager = fileManager;
    }

    public void manage(Set<String> ports) {
        fileManager.writeToPrometheusYaml(jobTemplate(ports));
    }

    public static String jobTemplate(Set<String> ports) {
        Set<String> sockets = ports
                .stream()
                .map(port -> String.format("'host.docker.internal:%s'", port))
                .collect(Collectors.toSet());
        return String.format("""
                scrape_configs:
                    - job_name: 'backend'
                      metrics_path: '/actuator/prometheus'
                      scrape_interval: 3s
                      static_configs:
                        - targets: [%s]
                          labels:
                            application: 'app'""", String.join(",", sockets));
    }
}
