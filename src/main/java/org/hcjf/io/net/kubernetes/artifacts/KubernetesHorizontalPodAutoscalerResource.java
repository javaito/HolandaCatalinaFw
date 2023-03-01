package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V2beta2HorizontalPodAutoscaler;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.Introspection;

import java.util.Map;

public class KubernetesHorizontalPodAutoscalerResource extends KubernetesArtifactResource<V2beta2HorizontalPodAutoscaler> {

    public static final String NAME = "system_k8s_hpa";

    @Override
    public String getImplName() {
        return NAME;
    }

    @Override
    protected Class<V2beta2HorizontalPodAutoscaler> getArtifactType() {
        return V2beta2HorizontalPodAutoscaler.class;
    }

    @Override
    protected void createArtifact(V2beta2HorizontalPodAutoscaler artifact, Map<String, Object> rawArtifact) {
        try {
            String pretty = Introspection.resolve(rawArtifact, Fields.PRETTY);
            String dryRun = Introspection.resolve(rawArtifact, Fields.DRY_RUN);
            String fieldManager = Introspection.resolve(rawArtifact, Fields.FIELD_MANAGER);
            String fieldValidation = Introspection.resolve(rawArtifact, Fields.FIELD_VALIDATION);
            getAutoscalingV2beta2Api().createNamespacedHorizontalPodAutoscaler(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s horizontal pod autoscaler creation fail", ex);
        }
    }
}
