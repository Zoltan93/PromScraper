package com.zhorvat.quarkus.prometheus;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.compress.utils.Sets;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class PodWatcher {

    @Inject
    private JobManager jobManager;

    @Scheduled(initialDelayString = "10000",fixedRateString = "10000")
    public void watch() throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listPodForAllNamespaces().execute();
        for (V1Pod item : list.getItems()) {
            jobManager.manage(item.getMetadata().getNamespace());
        }
    }
}
