package org.hcjf.io.net.kubernetes;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.*;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layer;
import org.hcjf.layers.crud.CreateLayerInterface;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.*;

public class KubernetesSpyResource extends Layer implements CreateLayerInterface<Map<String,Object>>, ReadRowsLayerInterface {

    private static final class Kinds {
        private static final String JOB = "Job";
        private static final String CONFIG_MAP = "ConfigMap";
    }

    private static final class Fields {

        private static final String KIND = "kind";

        private static final class ConfigMap {
            private static final String API_VERSION = "v1";
            private static final String NAME = "name";
            private static final String DATA = "data";
        }

        private static final class Job {
            private static final String API_VERSION = "batch/v1";
            private static final String NAME = "name";
            private static final String REPLICAS = "replicas";
            private static final String RESTART_POLICY = "restartPolicy";
            private static final String CONTAINERS = "containers";
            private static final String VOLUMES = "volumes";
        }

        private static final class Volume {
            private static final String NAME = "name";
            private static final String CONFIG_MAP_NAME = "configMapName";
            private static final String SECRET_NAME = "secretName";
        }

        private static final class VolumeMount {
            private static final String NAME = "name";
            private static final String MOUNT_PATH = "mountPath";
        }

        private static final class Container {
            private static final String NAME = "name";
            private static final String IMAGE = "image";
            private static final String COMMAND = "command";
            private static final String ARGS = "args";
            private static final String ENVIRONMENTS = "environments";
            private static final String VOLUME_MOUNTS = "volumeMounts";
        }

        private static final class EnvFromSource {
            private static final String KIND = "kind";
            private static final String NAME = "name";
        }
    }

    public static final String NAME = "system_k8s";
    public static final String CONFIG_MAP = "system_k8s_config_map";
    public static final String NAMESPACED_CONFIG_MAP = "system_k8s_namespaced_config_map";
    public static final String END_POINT = "system_k8s_end_point";
    public static final String NAMESPACED_END_POINT = "system_k8s_namespaced_end_point";
    public static final String EVENT = "system_k8s_event";
    public static final String NAMESPACED_EVENT = "system_k8s_namespaced_event";
    public static final String LIMIT_RANGE = "system_k8s_limit_range";
    public static final String NAMESPACE = "system_k8s_namespace";
    public static final String NODE = "system_k8s_node";
    public static final String PERSISTENT_VOLUME = "system_k8s_persistent_volume";
    public static final String PERSISTENT_VOLUME_CLAIM = "system_k8s_persistent_volume_claim";
    public static final String POD = "system_k8s_pod";
    public static final String NAMESPACED_POD = "system_k8s_namespaced_pod";
    public static final String POD_TEMPLATE = "system_k8s_pod_template";
    public static final String NAMESPACED_POD_TEMPLATE = "system_k8s_namespaced_pod_template";
    public static final String REPLICATION_CONTROLLER = "system_k8s_replication_controller";
    public static final String RESOURCE_QUOTA = "system_k8s_resource_quota";
    public static final String NAMESPACED_RESOURCE_QUOTA = "system_k8s_namespaced_resource_quota";
    public static final String SECRET = "system_k8s_secret";
    public static final String NAMESPACED_SECRET = "system_k8s_namespaced_secret";
    public static final String SERVICE_ACCOUNT = "system_k8s_service_account";
    public static final String NAMESPACED_SERVICE_ACCOUNT = "system_k8s_namespaced_service_account";
    public static final String SERVICE = "system_k8s_service";
    public static final String NAMESPACED_SERVICE = "system_k8s_namespaced_service";
    public static final String COMPONENT_STATUS = "system_k8s_component_status";

    private final ApiClient client;
    private final BatchV1Api batchApi;
    private final CoreV1Api api;

    public KubernetesSpyResource() {
        super(NAME);
        try {
            client = Config.fromCluster();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        Configuration.setDefaultApiClient(client);
        this.api = new CoreV1Api();
        this.batchApi = new BatchV1Api();
    }

    @Override
    public Set<String> getAliases() {
        return Set.of(
                CONFIG_MAP,
                NAMESPACED_CONFIG_MAP,
                END_POINT,
                NAMESPACED_END_POINT,
                EVENT,
                NAMESPACED_EVENT,
                LIMIT_RANGE,
                NAMESPACE,
                NODE,
                PERSISTENT_VOLUME,
                PERSISTENT_VOLUME_CLAIM,
                POD,
                NAMESPACED_POD,
                POD_TEMPLATE,
                NAMESPACED_POD_TEMPLATE,
                REPLICATION_CONTROLLER,
                RESOURCE_QUOTA,
                NAMESPACED_RESOURCE_QUOTA,
                SECRET,
                NAMESPACED_SECRET,
                SERVICE_ACCOUNT,
                NAMESPACED_SERVICE_ACCOUNT,
                SERVICE,
                NAMESPACED_SERVICE,
                COMPONENT_STATUS
        );
    }

    @Override
    public Map<String, Object> create(Map<String, Object> object) {
        Map<String,Object> result;
        if(!object.containsKey(Fields.KIND)) {
            throw new HCJFRuntimeException("Unable to create some kubernetes artifact if 'kind' field is not present");
        }
        String kind = (String) object.get(Fields.KIND);
        switch (kind) {
            case Kinds.JOB: {
                result = Introspection.toMap(createJob(object));
                break;
            }
            case Kinds.CONFIG_MAP: {
                result = Introspection.toMap(createConfigMap(object));
                break;
            }
            default:{
                throw new HCJFRuntimeException("Unrecognized kubernetes artifact '%s'", kind);
            }
        }
        return result;
    }

    private V1Job createJob(Map<String,Object> jobDefinition) {
        V1Job job;
        try {
            job = new V1JobBuilder().
                withApiVersion(Fields.Job.API_VERSION).
                withKind((String) jobDefinition.get(Fields.KIND)).
                withMetadata(new V1ObjectMetaBuilder().
                    withName((String) jobDefinition.get(Fields.Job.NAME)).
                    withNamespace(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE)).build()).
                withSpec(new V1JobSpecBuilder().
                    withTemplate(new V1PodTemplateSpecBuilder().
                        withMetadata(new V1ObjectMetaBuilder().
                            withName((String) jobDefinition.get(Fields.Job.NAME)).
                            withNamespace(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE)).build()
                        ).
                        withSpec(new V1PodSpecBuilder().
                            withVolumes(getVolumes((Collection<Map<String, Object>>) jobDefinition.get(Fields.Job.VOLUMES))).
                            withRestartPolicy((String) jobDefinition.get(Fields.Job.RESTART_POLICY)).
                            withContainers(getContainers((Collection<Map<String, Object>>) jobDefinition.get(Fields.Job.CONTAINERS))).build()
                        ).build()
                    ).build()
                ).build();

            Integer replicas = (Integer) jobDefinition.get(Fields.Job.REPLICAS);
            for (int i = 0; i < replicas; i++) {
                batchApi.createNamespacedJob(
                        SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                        job, null, null, null);
            }
        } catch (ApiException ex) {
            throw new HCJFRuntimeException("Unable to create job, '%s'", ex, ex.getResponseBody());
        }
        return job;
    }

    private List<V1Container> getContainers(Collection<Map<String,Object>> definition) {
        List<V1Container> result = new ArrayList<>();

        if(definition != null) {
            for(Map<String,Object> container : definition) {
                result.add(new V1ContainerBuilder().
                        withEnvFrom(getEvnFromSources((Collection<Map<String, Object>>) container.get(Fields.Container.ENVIRONMENTS))).
                        withName((String) container.get(Fields.Container.NAME)).
                        withImage((String) container.get(Fields.Container.IMAGE)).
                        withCommand((List<String>) container.get(Fields.Container.COMMAND)).
                        withArgs((List<String>) container.get(Fields.Container.ARGS)).
                        withVolumeMounts(getVolumeMounts((Collection<Map<String, Object>>) container.get(Fields.Container.VOLUME_MOUNTS))).build());
            }
        }

        return result;
    }

    private List<V1EnvFromSource> getEvnFromSources(Collection<Map<String,Object>> definition) {
        List<V1EnvFromSource> result = new ArrayList<>();

        if(definition != null) {
            for(Map<String,Object> env : definition) {
                result.add(new V1EnvFromSourceBuilder().withConfigMapRef(
                        new V1ConfigMapEnvSourceBuilder().withName((String) env.get(Fields.EnvFromSource.NAME))
                        .build()).
                build());
            }
        }

        return result;
    }

    private List<V1VolumeMount> getVolumeMounts(Collection<Map<String,Object>> definition) {
        List<V1VolumeMount> result = new ArrayList<>();

        if(definition != null) {
            for(Map<String,Object> vm : definition) {
                result.add(new V1VolumeMountBuilder().
                        withName((String) vm.get(Fields.VolumeMount.NAME)).
                        withMountPath((String) vm.get(Fields.VolumeMount.MOUNT_PATH)).
                        build());
            }
        }

        return result;
    }

    private List<V1Volume> getVolumes(Collection<Map<String,Object>> definition) {
        List<V1Volume> result = new ArrayList<>();

        if(definition != null) {
            for(Map<String,Object> v : definition) {
                if(v.containsKey(Fields.Volume.CONFIG_MAP_NAME)) {
                    result.add(new V1VolumeBuilder().
                            withName((String) v.get(Fields.Volume.NAME)).
                            withConfigMap(new V1ConfigMapVolumeSourceBuilder().
                                    withName((String) v.get(Fields.Volume.CONFIG_MAP_NAME)).
                                    build()).
                            build());
                } else if(v.containsKey(Fields.Volume.SECRET_NAME)) {
                    result.add(new V1VolumeBuilder().
                            withName((String) v.get(Fields.Volume.NAME)).
                            withSecret(new V1SecretVolumeSourceBuilder().
                                    withSecretName((String) v.get(Fields.Volume.SECRET_NAME)).
                                    build()).
                            build());
                }
            }
        }

        return result;
    }

    private V1ConfigMap createConfigMap(Map<String,Object> configMapDefinition) {
        V1ConfigMap configMap;

        try {
            configMap = new V1ConfigMapBuilder().
                    withApiVersion(Fields.ConfigMap.API_VERSION).
                    withKind((String) configMapDefinition.get(Fields.KIND)).
                    withMetadata(new V1ObjectMetaBuilder().
                            withName((String) configMapDefinition.get(Fields.ConfigMap.NAME)).
                            withNamespace(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE)).
                            build()).
                    withData((Map<String, String>) configMapDefinition.get(Fields.ConfigMap.DATA)).
                    build();

            api.createNamespacedConfigMap(
                    SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                    configMap, null, null, null);
        } catch (ApiException ex){
            throw new HCJFRuntimeException("Unable to create config map", ex);
        }

        return configMap;
    }

    @Override
    public Collection<JoinableMap> readRows(Queryable queryable) {
        Collection<JoinableMap> result = new ArrayList<>();
        KubernetesBeanConsumer consumer = new KubernetesBeanConsumer();
        try {
            switch (queryable.getResourceName()) {
                case CONFIG_MAP: {
                    for(V1ConfigMap configMap : api.listConfigMapForAllNamespaces(
                            null, null, null, null, null, null, null, null, null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(configMap, consumer)));
                    }
                    break;
                }
                case NAMESPACED_CONFIG_MAP: {
                    for(V1ConfigMap configMap : api.listNamespacedConfigMap(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null, null, null, null, null, null, null, null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(configMap, consumer)));
                    }
                    break;
                }
                case END_POINT: {
                    for(V1Endpoints endpoints : api.listEndpointsForAllNamespaces(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(endpoints, consumer)));
                    }
                    break;
                }
                case NAMESPACED_END_POINT: {
                    for(V1Endpoints endpoints : api.listNamespacedEndpoints(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(endpoints, consumer)));
                    }
                    break;
                }
                case EVENT: {
                    for(V1Event event : api.listEventForAllNamespaces(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(event, consumer)));
                    }
                    break;
                }
                case NAMESPACED_EVENT: {
                    for(V1Event event : api.listNamespacedEvent(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(event, consumer)));
                    }
                    break;
                }
                case LIMIT_RANGE: {
                    for(V1LimitRange limitRange : api.listNamespacedLimitRange(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(limitRange, consumer)));
                    }
                    break;
                }
                case NAMESPACE: {
                    for(V1Namespace namespace : api.listNamespace(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(namespace, consumer)));
                    }
                    break;
                }
                case NODE: {
                    for(V1Node node : api.listNode(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(node, consumer)));
                    }
                    break;
                }
                case PERSISTENT_VOLUME: {
                    for(V1PersistentVolume persistentVolume : api.listPersistentVolume(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(persistentVolume, consumer)));
                    }
                    break;
                }
                case PERSISTENT_VOLUME_CLAIM: {
                    for(V1PersistentVolumeClaim persistentVolumeClaim : api.listNamespacedPersistentVolumeClaim(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(persistentVolumeClaim, consumer)));
                    }
                    break;
                }
                case POD: {
                    for(V1Pod pod : api.listPodForAllNamespaces(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(pod, consumer)));
                    }
                    break;
                }
                case NAMESPACED_POD: {
                    for(V1Pod pod : api.listNamespacedPod(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(pod, consumer)));
                    }
                    break;
                }
                case POD_TEMPLATE: {
                    for(V1PodTemplate podTemplate : api.listNamespacedPodTemplate(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(podTemplate, consumer)));
                    }
                    break;
                }
                case NAMESPACED_POD_TEMPLATE: {
                    for(V1PodTemplate podTemplate : api.listPodTemplateForAllNamespaces(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(podTemplate, consumer)));
                    }
                    break;
                }
                case REPLICATION_CONTROLLER: {
                    for(V1ReplicationController replicationController : api.listNamespacedReplicationController(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(replicationController, consumer)));
                    }
                    break;
                }
                case RESOURCE_QUOTA: {
                    for(V1ResourceQuota resourceQuota : api.listResourceQuotaForAllNamespaces(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(resourceQuota, consumer)));
                    }
                    break;
                }
                case NAMESPACED_RESOURCE_QUOTA: {
                    for(V1ResourceQuota resourceQuota : api.listNamespacedResourceQuota(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(resourceQuota, consumer)));
                    }
                    break;
                }
                case SECRET: {
                    for(V1Secret secret : api.listSecretForAllNamespaces(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(secret, consumer)));
                    }
                    break;
                }
                case NAMESPACED_SECRET: {
                    for(V1Secret secret : api.listNamespacedSecret(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(secret, consumer)));
                    }
                    break;
                }
                case SERVICE_ACCOUNT: {
                    for(V1ServiceAccount serviceAccount : api.listServiceAccountForAllNamespaces(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(serviceAccount, consumer)));
                    }
                    break;
                }
                case NAMESPACED_SERVICE_ACCOUNT: {
                    for(V1ServiceAccount serviceAccount : api.listNamespacedServiceAccount(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(serviceAccount, consumer)));
                    }
                    break;
                }
                case SERVICE: {
                    for(V1Service service : api.listServiceForAllNamespaces(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(service, consumer)));
                    }
                    break;
                }
                case NAMESPACED_SERVICE: {
                    for(V1Service service : api.listNamespacedService(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(service, consumer)));
                    }
                    break;
                }
                case COMPONENT_STATUS: {
                    for(V1ComponentStatus status : api.listComponentStatus(null, null, null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(status, consumer)));
                    }
                    break;
                }
            }
        }catch (Exception ex){
            throw new RuntimeException("Kubernetes resource error", ex);
        }

        return queryable.evaluate(result);
    }

    private static class KubernetesBeanConsumer implements Introspection.Consumer {

        private static final String K8S_PACKAGE_NAME = "io.kubernetes";

        @Override
        public Object consume(Object value) {
            Object result;
            if(value instanceof DateTime) {
                result = ((DateTime)value).toGregorianCalendar().getTime();
            } else if(value.getClass().getPackage().getName().startsWith(K8S_PACKAGE_NAME)) {
                result = Introspection.toMap(value, this);
            } else if(value instanceof Collection) {
                Collection newCollection = new ArrayList();
                for(Object collectionValue : ((Collection)value)) {
                    newCollection.add(consume(collectionValue));
                }
                result = newCollection;
            } else {
                result = value;
            }
            return result;
        }
    }
}
