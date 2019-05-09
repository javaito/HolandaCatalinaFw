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

import java.io.IOException;
import java.util.*;

public class KubernetesSpyResource extends Layer implements CreateLayerInterface<Map<String,Object>>, ReadRowsLayerInterface {

    private static final class Kinds {
        private static final String JOB = "Job";
    }

    private static final class Fields {

        private static final String KIND = "kind";

        private static final class Job {
            private static final String API_VERSION = "batch/v1";
            private static final String NAME = "name";
            private static final String RESTART_POLICY = "restartPolicy";
        }

        private static final class Containers {
            private static final String NAME = "containerName";
            private static final String IMAGE = "containerImage";
            private static final String COMMAND = "containerCommand";
            private static final String ARGS = "containerArgs";
        }
    }

    public static final String NAME = "system_k8s";
    public static final String CONFIG_MAP = "system_k8s_config_map";
    public static final String END_POINT = "system_k8s_end_point";
    public static final String EVENT = "system_k8s_event";
    public static final String LIMIT_RANGE = "system_k8s_limit_range";
    public static final String NAMESPACE = "system_k8s_namespace";
    public static final String NODE = "system_k8s_node";
    public static final String PERSISTENT_VOLUME = "system_k8s_persistent_volume";
    public static final String PERSISTENT_VOLUME_CLAIM = "system_k8s_persistent_volume_claim";
    public static final String POD = "system_k8s_pod";
    public static final String POD_TEMPLATE = "system_k8s_pod_template";
    public static final String REPLICATION_CONTROLLER = "system_k8s_replication_controller";
    public static final String RESOURCE_QUOTA = "system_k8s_resource_quota";
    public static final String SECRET = "system_k8s_secret";
    public static final String SERVICE_ACCOUNT = "system_k8s_service_account";
    public static final String SERVICE = "system_k8s_service";

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
                END_POINT,
                EVENT,
                LIMIT_RANGE,
                NAMESPACE,
                NODE,
                PERSISTENT_VOLUME,
                PERSISTENT_VOLUME_CLAIM,
                POD,
                POD_TEMPLATE,
                REPLICATION_CONTROLLER,
                RESOURCE_QUOTA,
                SECRET,
                SERVICE_ACCOUNT,
                SERVICE
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
            default:{
                throw new HCJFRuntimeException("Unrecognized kubernetes artifact '%s'", kind);
            }
        }
        return result;
    }

    public V1Job createJob(Map<String,Object> object) {
        V1Job job;
        try {
            job = new V1JobBuilder().
                withApiVersion(Fields.Job.API_VERSION).
                withKind((String) object.get(Fields.KIND)).
                withMetadata(new V1ObjectMetaBuilder().
                    withName((String) object.get(Fields.Job.NAME)).
                    withNamespace(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE)).build()).
                withSpec(new V1JobSpecBuilder().withTemplate(new V1PodTemplateSpecBuilder().
                    withMetadata(new V1ObjectMetaBuilder().
                        withName((String) object.get(Fields.Job.NAME)).
                        withNamespace(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE)).build()
                    ).
                    withSpec(new V1PodSpecBuilder().
                        withRestartPolicy((String) object.get(Fields.Job.RESTART_POLICY)).
                        withContainers(new V1ContainerBuilder().
                            withName((String) object.get(Fields.Containers.NAME)).
                            withImage((String) object.get(Fields.Containers.IMAGE)).
                            withCommand((List<String>) object.get(Fields.Containers.COMMAND)).
                            withArgs((String) object.get(Fields.Containers.ARGS)).build()
                        ).build()
                    ).build()
                ).build()
            ).build();


            batchApi.createNamespacedJob(
                    SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                    job, null, null, null);
        } catch (ApiException ex) {
            throw new HCJFRuntimeException("Unable to create job", ex);
        }
        return job;
    }

    @Override
    public Collection<JoinableMap> readRows(Queryable queryable) {
        Collection<JoinableMap> result = new ArrayList<>();
        try {
            switch (queryable.getResourceName()) {
                case CONFIG_MAP: {
                    for(V1ConfigMap configMap : api.listNamespacedConfigMap(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null, null, null, null, null, null, null, null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(configMap)));
                    }
                    break;
                }
                case END_POINT: {
                    for(V1Endpoints endpoints : api.listNamespacedEndpoints(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(endpoints)));
                    }
                    break;
                }
                case EVENT: {
                    for(V1Event event : api.listNamespacedEvent(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(event)));
                    }
                    break;
                }
                case LIMIT_RANGE: {
                    for(V1LimitRange limitRange : api.listNamespacedLimitRange(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(limitRange)));
                    }
                    break;
                }
                case NAMESPACE: {
                    for(V1Namespace namespace : api.listNamespace(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(namespace)));
                    }
                    break;
                }
                case NODE: {
                    for(V1Node node : api.listNode(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(node)));
                    }
                    break;
                }
                case PERSISTENT_VOLUME: {
                    for(V1PersistentVolume persistentVolume : api.listPersistentVolume(
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(persistentVolume)));
                    }
                    break;
                }
                case PERSISTENT_VOLUME_CLAIM: {
                    for(V1PersistentVolumeClaim persistentVolumeClaim : api.listNamespacedPersistentVolumeClaim(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(persistentVolumeClaim)));
                    }
                    break;
                }
                case POD: {
                    for(V1Pod pod : api.listNamespacedPod(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(pod)));
                    }
                    break;
                }
                case POD_TEMPLATE: {
                    for(V1PodTemplate podTemplate : api.listNamespacedPodTemplate(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(podTemplate)));
                    }
                    break;
                }
                case REPLICATION_CONTROLLER: {
                    for(V1ReplicationController replicationController : api.listNamespacedReplicationController(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(replicationController)));
                    }
                    break;
                }
                case RESOURCE_QUOTA: {
                    for(V1ResourceQuota resourceQuota : api.listNamespacedResourceQuota(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(resourceQuota)));
                    }
                    break;
                }
                case SECRET: {
                    for(V1Secret secret : api.listNamespacedSecret(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(secret)));
                    }
                    break;
                }
                case SERVICE_ACCOUNT: {
                    for(V1ServiceAccount serviceAccount : api.listNamespacedServiceAccount(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(serviceAccount)));
                    }
                    break;
                }
                case SERVICE: {
                    for(V1Service service : api.listNamespacedService(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE),
                            null, null,null,null,null,null,null,null,null).getItems()) {
                        result.add(new JoinableMap(Introspection.toMap(service)));
                    }
                    break;
                }
            }
        }catch (Exception ex){
            throw new RuntimeException("Kubernetes resource error", ex);
        }

        return queryable.evaluate(result);
    }


}
