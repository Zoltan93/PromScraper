package com.zhorvat.quarkus.prometheus;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

@ApplicationScoped
public class JobManager {

    @Inject
    private PodWatcher podWatcher;
    @Inject
    private FileManager fileManager;

    private final String fileName;

    public JobManager(
            @ConfigProperty(name = "prometheusFile.name") String fileName
    ) {
        this.fileName = fileName;
    }

    @Scheduled(cron = "*/5 * * * * *")
    void manage() {
        Set<String> pods = podWatcher.getPods();
        pods.stream()
                .filter(pod -> fileManager.isPodPresent(pod))
                .forEach(pod -> fileManager.writeToFile(fileName, jobTemplate(pod)));
    }

    private String jobTemplate(String jobName) {
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
