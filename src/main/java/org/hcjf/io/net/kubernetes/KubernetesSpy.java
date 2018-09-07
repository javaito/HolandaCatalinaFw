package org.hcjf.io.net.kubernetes;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.hcjf.io.net.http.HttpClient;
import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;

import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * This service spy periodically the kubernetes cluster in order to knows all the updates into the cluster.
 * @author javaito
 */
public final class KubernetesSpy extends Service<KubernetesSpyConsumer> {

    private static final KubernetesSpy instance;
    private final Collection<KubernetesSpyConsumer> consumers;

    static {
        instance = new KubernetesSpy();
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
        run(new KubernetesSpyTask(), ServiceSession.getSystemSession());
    }

    /**
     * This task spy periodically the kubernetes cluster and generate events when discovery some update into the cluster.
     */
    private class KubernetesSpyTask implements Runnable {

        @Override
        public void run() {

            //KUBE_TOKEN=$(</var/run/secrets/kubernetes.io/serviceaccount/token) #
            //curl -sSk -H "Authorization: Bearer $KUBE_TOKEN" https://$KUBERNETES_SERVICE_HOST:$KUBERNETES_PORT_443_TCP_PORT/api/v1/namespaces/default/pods/$HOSTNAME

            ApiClient client = null;
            CoreV1Api api = null;

            String token = null;
            String url = null;

            try {
                token = new String(Files.readAllBytes(
                        SystemProperties.getPath(SystemProperties.Net.KubernetesSpy.TOKEN_FILE_PATH)));

                url = String.format(SystemProperties.get(SystemProperties.Net.KubernetesSpy.REST_URL),
                        System.getenv(SystemProperties.get(SystemProperties.Net.KubernetesSpy.MASTER_NODE_HOST)),
                        System.getenv(SystemProperties.get(SystemProperties.Net.KubernetesSpy.MASTER_NODE_PORT)),
                        System.getenv(SystemProperties.get(SystemProperties.Net.KubernetesSpy.HOST_NAME)));

                //Try to connect with the cluster.
                client = Config.defaultClient();
                client.getHttpClient().setConnectTimeout(
                        SystemProperties.getLong(SystemProperties.Net.KubernetesSpy.CLIENT_CONNECTION_TIMEOUT), TimeUnit.MILLISECONDS);
                client.setVerifyingSsl(false);
                api = new CoreV1Api(client);
            } catch (Exception ex) {
                Log.e(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "Unable to start kubernetes spy", ex);
            }

            if(client != null) {
                while(!Thread.currentThread().isInterrupted()) {
                    try {

                        System.out.println("############URL: " + url);

                        HttpClient httpClient = new HttpClient(new URL(url));
                        httpClient.setHttpsInsecureConnection(true);
                        httpClient.addHttpHeader(new HttpHeader(HttpHeader.AUTHORIZATION,
                                String.format(SystemProperties.get(SystemProperties.Net.KubernetesSpy.AUTHORIZATION_HEADER), token)));
                        HttpResponse response = httpClient.request();
                        System.out.println("#############Body" + new String(response.getBody()));

//                        //TODO: Add many others events in this task and methods to call into the observer for each new event
//
//                        //Spy all the pods into the kubernetes cluster
//                        V1PodList list = api.listPodForAllNamespaces(
//                                null, null, null,
//                                null, null, null, null,
//                                null, null);
//                        for (V1Pod pod : list.getItems()) {
//                            for(KubernetesSpyConsumer consumer : consumers) {
//                                if(consumer.getInitTimestamp() < pod.getMetadata().getCreationTimestamp().getMillis() &&
//                                        consumer.getFilter().filter(pod)) {
//                                    if(pod.getMetadata().getDeletionTimestamp() != null) {
//                                        consumer.onLostPod(pod);
//                                    } else {
//                                        consumer.onDiscoveryPod(pod);
//                                    }
//                                }
//                            }
//                        }
                    } catch (Exception ex) {
                        Log.e(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "Kubernetes spy task exception", ex);
                    }

                    try {
                        Thread.sleep(SystemProperties.getLong(SystemProperties.Net.KubernetesSpy.TASK_SLEEP_TIME));
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }

            Log.d(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "Kubernetes spy task finish");
        }

    }
}
