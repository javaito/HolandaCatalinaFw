package org.hcjf.io.net.kubernetes;

import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceConsumer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author javaito
 */
public abstract class KubernetesSpyConsumer implements ServiceConsumer {

    private final Map<String, V1Pod> pods;
    private final Map<String, V1Service> services;
    private final PodMatcher podMatcher;
    private final ServiceMatcher serviceMatcher;

    public KubernetesSpyConsumer(PodMatcher podMatcher, ServiceMatcher serviceMatcher) {
        this.podMatcher = podMatcher;
        this.serviceMatcher = serviceMatcher;
        this.pods = new HashMap<>();
        this.services = new HashMap<>();
    }

    public KubernetesSpyConsumer(PodMatcher podMatcher) {
        this(podMatcher, (S)->false);
    }

    public KubernetesSpyConsumer(ServiceMatcher serviceMatcher) {
        this((P)->false, serviceMatcher);
    }

    public KubernetesSpyConsumer() {
        this((P)->true, (S)->true);
    }

    /**
     * This method is called periodically indicating all the current pod instances into the kubernetes cluster.
     * @param podList List of current pods.
     */
    public final void updatePods(V1PodList podList) {
        Log.d(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "Starting update pods process");
        Set<String> ids = new HashSet<>();
        ids.addAll(pods.keySet());
        for(V1Pod pod : podList.getItems()) {
            if(!ids.remove(pod.getMetadata().getUid())) {
                Log.d(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "New pod founded: %s", pod.getMetadata().getUid());
                pods.put(pod.getMetadata().getUid(), pod);
                if(podMatcher.match(pod)) {
                    Log.d(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG), "Adding new pod: %s", pod.getMetadata().getUid());
                    onPodDiscovery(pod);
                }
            }
        }

        for(String uid : ids) {
            onPodLost(pods.remove(uid));
        }
    }

    /**
     * This method is called periodically indicating all the current service instances into the kubernetes cluster.
     * @param serviceList List of the current services.
     */
    public final void updateServices(V1ServiceList serviceList) {
        Set<String> ids = new HashSet<>();
        ids.addAll(services.keySet());
        for(V1Service service : serviceList.getItems()) {
            if(!ids.remove(service.getMetadata().getUid())) {
                if(!service.getSpec().getPorts().isEmpty()) {
                    services.put(service.getMetadata().getUid(), service);
                    if (serviceMatcher.match(service)) {
                        onServiceDiscovery(service);
                    }
                } else {
                    Log.d(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG),
                            "Service skip because there aren't end points present: %s", service.getMetadata().getName());
                }
            } else {
                if(service.getSpec().getPorts().isEmpty()) {
                    Log.d(SystemProperties.get(SystemProperties.Net.KubernetesSpy.LOG_TAG),
                            "Service lost because there aren't end points present: %s", service.getMetadata().getName());
                    onServiceLost(services.remove(service.getMetadata().getUid()));
                }
            }
        }

        for(String uid : ids) {
            onServiceLost(services.remove(uid));
        }
    }

    /**
     * This method is called when a new pod is discovery.
     * @param pod Discovery pod instance.
     */
    protected void onPodDiscovery(V1Pod pod) {}

    /**
     * This method is called when a pod is not more into the kubernetes cluster.
     * @param pod Lost pod instance.
     */
    protected void onPodLost(V1Pod pod) {}

    /**
     * This method is called when a new service is discovery.
     * @param service Discovery service instance.
     */
    protected void onServiceDiscovery(V1Service service) {};

    /**
     * This method is called when a service is not more into the kubernetes cluster.
     * @param service Lost service instance.
     */
    protected void onServiceLost(V1Service service) {};

    /**
     * This kind of matcher verify the conditions over the pod instance.
     */
    public interface PodMatcher {

        boolean match(V1Pod pod);

    }

    /**
     * This kind of matcher verify the conditions over the service instance.
     */
    public interface ServiceMatcher {

        boolean match(V1Service service);

    }
}
