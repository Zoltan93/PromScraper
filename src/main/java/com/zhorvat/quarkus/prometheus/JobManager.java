package com.zhorvat.quarkus.prometheus;

import java.util.Set;
import java.util.stream.Collectors;

public class JobManager {

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
