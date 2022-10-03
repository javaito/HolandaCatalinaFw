package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V1ConfigMap;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.Introspection;

import java.util.Map;

public class KubernetesConfigMapResource extends KubernetesArtifactResource<V1ConfigMap> {

    public static final String NAME = "system_k8s_config_map";

    @Override
    public String getImplName() {
        return NAME;
    }

    @Override
    protected Class<V1ConfigMap> getArtifactType() {
        return V1ConfigMap.class;
    }

    @Override
    protected void createArtifact(V1ConfigMap artifact, Map<String, Object> rawArtifact) {
        try {
            String pretty = Introspection.resolve(rawArtifact, Fields.PRETTY);
            String dryRun = Introspection.resolve(rawArtifact, Fields.DRY_RUN);
            String fieldManager = Introspection.resolve(rawArtifact, Fields.FIELD_MANAGER);
            String fieldValidation = Introspection.resolve(rawArtifact, Fields.FIELD_VALIDATION);
            getCoreApi().createNamespacedConfigMap(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s config map creation fail", ex);
        }
    }
}
