package com.zhorvat.quarkus.prometheus;

import com.zhorvat.quarkus.file.FileManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JobManager {

    @Inject
    private FileManager fileManager;

    private final String fileName;

    public JobManager(
            @ConfigProperty(name = "prometheusFile.name") String fileName
    ) {
        this.fileName = fileName;
    }

    public void manage(String pod) {
        if (!fileManager.isPodPresent(pod)) {
            fileManager.writeToFile(fileName, jobTemplate(pod));
        }
    }

    public static String jobTemplate(String jobName) {
        return String.format("""
                - job_name: '%s'
                  metrics_path: '/actuator/prometheus'
                  scrape_interval: 3s
                  static_configs:
                    - targets: [ 'host.docker.internal:8081']
                      labels:
                        application: 'app'""", jobName);
    }
}
