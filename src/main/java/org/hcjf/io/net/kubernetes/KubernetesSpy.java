package org.hcjf.io.net.kubernetes;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.Config;
import org.hcjf.layers.Layers;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * This service spy periodically the kubernetes cluster in order to knows all the updates into the cluster.
 * @author javaito
 */
public final class KubernetesSpy extends Service<KubernetesSpyConsumer> {

    private static final KubernetesSpy instance;
    private final Collection<KubernetesSpyConsumer> consumers;

    static {
        instance = new KubernetesSpy();

        Layers.publishLayer(KubernetesSpyResource.class);
    }

    /**
     * Service constructor.
     * @throws NullPointerException If the name is null.
     */
    private KubernetesSpy() {
        super(SystemProperties.get(SystemProperties.Net.KubernetesSpy.SERVICE_NAME), 2);
        this.consumers = new ArrayList<>();
    }

    /**
     * Returns the singleton instance.
     * @return Kubernetes spy instance.
     */
    public static KubernetesSpy getInstance() {
        return instance;
    }

    public static String getHostName() {
        return System.getenv("HOSTNAME");
    }

    /**
     * Register a consumer into the spy.
     * @param consumer Object with the logic to consume the service.
     */
    @Override
    public void registerConsumer(KubernetesSpyConsumer consumer) {
        Objects.requireNonNull(consumer, "Unsupported null consumer");
        consumers.add(consumer);
    }

    /**
     * Unregister a consumer of the spy.
     * @param consumer Consumer to unregister.
     */
    @Override
    public void unregisterConsumer(KubernetesSpyConsumer consumer) {
        if(consumer != null) {
            consumers.remove(consumer);
        }
    }

    /**
     * Only start the spy task to verify
     */
    @Override
    protected void init() {
        run(new KubernetesPodSpyTask(), ServiceSession.getSystemSession());
    }

    /**
     * This task check periodically the changes in the list of pods into the kubernetes cluster.
     */
    private class KubernetesPodSpyTask implements Runnable {

        @Override
        public void run() {
            String namespace = null;
            ApiClient client = null;
            try {
                client = Config.fromCluster();
            } catch (IOException e) {
            }
            Configuration.setDefaultApiClient(client);
            CoreV1Api api = new CoreV1Api();

            try {
                namespace = new String(Files.readAllBytes(SystemProperties.getPath(SystemProperties.Net.KubernetesSpy.NAMESPACE_FILE_PATH))).trim();
            } catch (Exception ex){}

            while(!Thread.currentThread().isInterrupted()) {

                //List all the pods into the current name space.
                try {
                    V1PodList podList = api.listNamespacedPod(
                            namespace,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

                    for(KubernetesSpyConsumer consumer : consumers) {
                        fork(() -> {
                            consumer.updatePods(podList);
                        });
                    }
                } catch (Exception ex){
                    Log.w(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "Unable to list pods", ex);
                }

                try {
                    V1ServiceList serviceList = api.listNamespacedService(
                            namespace,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

                    for(KubernetesSpyConsumer consumer : consumers) {
                        fork(() -> {
                            consumer.updateServices(serviceList);
                        });
                    }
                } catch (Exception ex){
                    Log.w(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "Unable to list pods", ex);
                }

                try {
                    Thread.sleep(SystemProperties.getLong(SystemProperties.Net.KubernetesSpy.TASK_SLEEP_TIME));
                } catch (InterruptedException e) {
                    break;
                }
            }

            Log.d(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "Kubernetes spy task finish");
        }
    }
}
