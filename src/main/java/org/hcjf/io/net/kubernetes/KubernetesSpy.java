package org.hcjf.io.net.kubernetes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.io.net.kubernetes.beans.*;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

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
            JsonParser jsonParser = new JsonParser();
            String token = null;
            URL url = null;

            try {
                token = new String(Files.readAllBytes(
                        SystemProperties.getPath(SystemProperties.Net.KubernetesSpy.TOKEN_FILE_PATH)));

                url = new URL(String.format(SystemProperties.get(SystemProperties.Net.KubernetesSpy.REST_URL),
                        System.getenv(SystemProperties.get(SystemProperties.Net.KubernetesSpy.MASTER_NODE_HOST)),
                        System.getenv(SystemProperties.get(SystemProperties.Net.KubernetesSpy.MASTER_NODE_PORT)),
                        System.getenv(SystemProperties.get(SystemProperties.Net.KubernetesSpy.HOST_NAME))));
            } catch (Exception ex) {
                Log.e(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG),
                        "Unable to start kubernetes spy", ex);
            }

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    //TODO: Add many others events in this task and methods to call into the observer for each new event
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty(HttpHeader.AUTHORIZATION,
                            String.format(SystemProperties.get(SystemProperties.Net.KubernetesSpy.AUTHORIZATION_HEADER), token));
                    InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int readSize = inputStream.read(buffer);
                    while(readSize >= 0) {
                        byteArrayOutputStream.write(buffer, 0, readSize);
                        readSize = inputStream.read(buffer);
                    }
                    List<KubernetesBean> beans = getBeans(
                            jsonParser.parse(new String(byteArrayOutputStream.toByteArray())));
                    for(KubernetesBean bean : beans) {
                        if(bean instanceof Pod) {
                            publishPod((Pod) bean);
                        }
                    }

                    updateConsumers();
                } catch (Exception ex) {
                    Log.e(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG),
                            "Kubernetes spy task exception", ex);
                }

                try {
                    Thread.sleep(SystemProperties.getLong(SystemProperties.Net.KubernetesSpy.TASK_SLEEP_TIME));
                } catch (InterruptedException e) {
                    break;
                }
            }

            Log.d(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "Kubernetes spy task finish");
        }

        /**
         *
         * @param pod
         */
        private void publishPod(Pod pod) {
            for(KubernetesSpyConsumer consumer : consumers) {
                if(consumer.getLastUpdate() <= pod.getMetadata().getCreationTimestamp()) {
                    consumer.onDiscoveryPod(pod);
                }
            }
        }

        /**
         *
         */
        private void updateConsumers() {
            for(KubernetesSpyConsumer consumer : consumers) {
                consumer.setLastUpdate(System.currentTimeMillis());
            }
        }

        /**
         * Verify if the json element is a json object instance or is a json array instance and create
         * a list of pods from each json object into the array.
         * @param jsonElement Json element, expected json object or json array.
         * @return List with the pods instances for each json object.
         */
        private List<KubernetesBean> getBeans(JsonElement jsonElement) {
            List<KubernetesBean> result = new ArrayList<>();
            KubernetesBean bean;
            if(jsonElement instanceof JsonArray) {
                for(JsonElement arrayElement : ((JsonArray)jsonElement)) {
                    if(jsonElement instanceof JsonObject) {
                        bean = getBean((JsonObject) arrayElement);
                        if(bean != null) {
                            result.add(bean);
                        }
                    }
                }
            } else if(jsonElement instanceof JsonObject) {
                bean = getBean((JsonObject) jsonElement);
                if(bean != null) {
                    result.add(bean);
                }
            }
            return result;
        }

        /**
         * Returns a kubernetes bean instance created from the json object.
         * @param jsonObject Json object that contains the bean information.
         * @return Kubernetes bean instance, could be null.
         */
        private KubernetesBean getBean(JsonObject jsonObject) {
            KubernetesBean result = null;
            if(jsonObject.has(KubernetesBean.Fields.KIND)) {
                String kind = jsonObject.get(KubernetesBean.Fields.KIND).getAsString();
                if (kind.equals(Pod.class.getSimpleName())) {
                    result = getPod(jsonObject);
                }
            }
            return result;
        }

        /**
         *
         * @param jsonObject
         * @return
         */
        private Pod getPod(JsonObject jsonObject) {
            Pod pod = new Pod();
            if(jsonObject.has(Pod.Fields.API_VERSION)) {
                pod.setApiVersion(jsonObject.get(Pod.Fields.API_VERSION).getAsString());
            }

            if(jsonObject.has(Pod.Fields.METADATA)) {
                //Creating the pod metadata instance
                JsonObject jsonMetadata = jsonObject.get(Pod.Fields.METADATA).getAsJsonObject();
                PodMetadata podMetadata = new PodMetadata();
                if (jsonMetadata.has(PodMetadata.Fields.NAME)) {
                    podMetadata.setName(jsonMetadata.get(PodMetadata.Fields.NAME).getAsString());
                }
                if (jsonMetadata.has(PodMetadata.Fields.GENERATE_NAME)) {
                    podMetadata.setGenerateName(jsonMetadata.get(PodMetadata.Fields.GENERATE_NAME).getAsString());
                }
                if (jsonMetadata.has(PodMetadata.Fields.NAMESPACE)) {
                    podMetadata.setNamespace(jsonMetadata.get(PodMetadata.Fields.NAMESPACE).getAsString());
                }
                if (jsonMetadata.has(PodMetadata.Fields.SELF_LINK)) {
                    podMetadata.setSelfLink(jsonMetadata.get(PodMetadata.Fields.SELF_LINK).getAsString());
                }
                if (jsonMetadata.has(PodMetadata.Fields.UID)) {
                    podMetadata.setUid(UUID.fromString(jsonMetadata.get(PodMetadata.Fields.UID).getAsString()));
                }
                if (jsonMetadata.has(PodMetadata.Fields.RESOURCE_VERSION)) {
                    podMetadata.setResourceVersion(jsonMetadata.get(PodMetadata.Fields.RESOURCE_VERSION).getAsString());
                }
                if (jsonMetadata.has(PodMetadata.Fields.CREATION_TIMESTAMP)) {
                    try {
                        podMetadata.setCreationTimestamp(SystemProperties.getDateFormat(SystemProperties.Net.KubernetesSpy.JSON_DATE_FORMAT).
                                parse(jsonMetadata.get(PodMetadata.Fields.CREATION_TIMESTAMP).getAsString()).getTime());
                    } catch (Exception ex) {
                    }
                }
                pod.setMetadata(podMetadata);
            }

            if(jsonObject.has(Pod.Fields.SPEC)) {
                JsonObject jsonSpec = jsonObject.get(Pod.Fields.SPEC).getAsJsonObject();
                PodSpec podSpec = new PodSpec();
                if(jsonSpec.has(PodSpec.Fields.RESTART_POLICY)) {
                    podSpec.setRestartPolicy(jsonSpec.get(PodSpec.Fields.RESTART_POLICY).getAsString());
                }
                if(jsonSpec.has(PodSpec.Fields.TERMINATION_GRACE_PERIOD_SECONDS)) {
                    podSpec.setTerminationGracePeriodSeconds(jsonSpec.get(PodSpec.Fields.TERMINATION_GRACE_PERIOD_SECONDS).getAsInt());
                }
                if(jsonSpec.has(PodSpec.Fields.DNS_POLICY)) {
                    podSpec.setDnsPolicy(jsonSpec.get(PodSpec.Fields.DNS_POLICY).getAsString());
                }
                if(jsonSpec.has(PodSpec.Fields.SERVICE_ACCOUNT_NAME)) {
                    podSpec.setServiceAccountName(jsonSpec.get(PodSpec.Fields.SERVICE_ACCOUNT_NAME).getAsString());
                }
                if(jsonSpec.has(PodSpec.Fields.SERVICE_ACCOUNT)) {
                    podSpec.setServiceAccount(jsonSpec.get(PodSpec.Fields.SERVICE_ACCOUNT).getAsString());
                }
                if(jsonSpec.has(PodSpec.Fields.NODE_NAME)) {
                    podSpec.setNodeName(jsonSpec.get(PodSpec.Fields.NODE_NAME).getAsString());
                }
                pod.setSpec(podSpec);
            }

            if(jsonObject.has(Pod.Fields.STATUS)) {
                JsonObject jsonStatus = jsonObject.get(Pod.Fields.STATUS).getAsJsonObject();
                PodStatus podStatus = new PodStatus();
                if(jsonStatus.has(PodStatus.Fields.PHASE)) {
                    podStatus.setPhase(jsonStatus.get(PodStatus.Fields.PHASE).getAsString());
                }
                if(jsonStatus.has(PodStatus.Fields.HOST_IP)) {
                    podStatus.setHostIp(jsonStatus.get(PodStatus.Fields.HOST_IP).getAsString());
                }
                if(jsonStatus.has(PodStatus.Fields.POD_IP)) {
                    podStatus.setPodIp(jsonStatus.get(PodStatus.Fields.POD_IP).getAsString());
                }
                if(jsonStatus.has(PodStatus.Fields.START_TIME)) {
                    try {
                        podStatus.setStartTime(SystemProperties.getDateFormat(SystemProperties.Net.KubernetesSpy.JSON_DATE_FORMAT).
                                parse(jsonStatus.get(PodStatus.Fields.START_TIME).getAsString()).getTime());
                    } catch (Exception ex) {
                    }
                }
                pod.setStatus(podStatus);
            }

            return pod;
        }
    }
}
