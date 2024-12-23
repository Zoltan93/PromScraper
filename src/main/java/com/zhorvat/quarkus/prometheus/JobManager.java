package com.zhorvat.quarkus.prometheus;

import com.zhorvat.quarkus.file.FileManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

    public void manage(String pod) {
        fileManager.writeToFile(fileName, jobTemplate(pod));
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
