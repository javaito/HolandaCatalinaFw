package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.AutoscalingV2beta2Api;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.util.Config;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layer;
import org.hcjf.layers.crud.CreateLayerInterface;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class KubernetesArtifactResource<T extends Object> extends Layer implements CreateLayerInterface<Map<String,Object>> {

    public static final class Fields {
        public static final String YAML = "yaml";
        public static final String PARAMETERS = "parameters";
        public static final String PRETTY = "pretty";
        public static final String DRY_RUN = "dryRun";
        public static final String FIELD_MANAGER = "fieldManager";
        public static final String FIELD_VALIDATION = "fieldValidation";
    }

    private final ApiClient client;
    private final CoreV1Api coreApi;
    private final AppsV1Api appsApi;
    private final BatchV1Api batchApi;
    private final AutoscalingV2beta2Api autoscalingV2beta2Api;

    public KubernetesArtifactResource() {
        try {
            client = Config.fromCluster();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create k8s client", e);
        }
        Configuration.setDefaultApiClient(client);
        this.coreApi = new CoreV1Api();
        this.appsApi = new AppsV1Api();
        this.batchApi = new BatchV1Api();
        this.autoscalingV2beta2Api = new AutoscalingV2beta2Api();
    }

    protected abstract Class<T> getArtifactType();

    protected abstract void createArtifact(T artifact, Map<String,Object> rawArtifact);

    protected final CoreV1Api getCoreApi() {
        return coreApi;
    }

    protected final AppsV1Api getAppsApi() {
        return appsApi;
    }

    protected final BatchV1Api getBatchApi() {
        return batchApi;
    }

    protected final AutoscalingV2beta2Api getAutoscalingV2beta2Api() {
        return autoscalingV2beta2Api;
    }

    protected final ApiClient getClient() {
        return client;
    }

    protected final String getNamespace() {
        return SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE);
    }

    @Override
    public Map<String, Object> create(Map<String, Object> artifact) {
        Map<String,Object> response = new HashMap<>();
        String yamlBody = Introspection.resolve(artifact, Fields.YAML);
        if (yamlBody == null) {
            throw new HCJFRuntimeException("The field 'yaml' is required");
        }

        List<String> parameters = Introspection.resolve(artifact, Fields.PARAMETERS);
        if (parameters != null && parameters.size() > 0) {
            yamlBody = String.format(yamlBody, parameters.toArray());
        }

        T artifactInstance = Yaml.loadAs(yamlBody, getArtifactType());
        createArtifact(artifactInstance, artifact);
        return response;
    }
}
