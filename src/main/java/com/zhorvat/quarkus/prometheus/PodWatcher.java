package com.zhorvat.quarkus.prometheus;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.compress.utils.Sets;

import java.io.IOException;
import java.util.Set;

@ApplicationScoped
public class PodWatcher {

    private final Set<String> pods = Sets.newHashSet();

    @Scheduled(cron = "*/5 * * * * *")
    private void watch() throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listPodForAllNamespaces().execute();
        for (V1Pod item : list.getItems()) {
            pods.add(item.getMetadata().getName());
        }
    }

    public Set<String> getPods() {
        return pods;
    }
}
