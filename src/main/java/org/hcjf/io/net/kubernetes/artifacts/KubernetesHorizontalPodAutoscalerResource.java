package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V1HorizontalPodAutoscaler;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.Introspection;

import java.util.Map;

public class KubernetesHorizontalPodAutoscalerResource extends KubernetesArtifactResource<V1HorizontalPodAutoscaler> {

    public static final String NAME = "system_k8s_hpa";

    @Override
    public String getImplName() {
        return NAME;
    }

    @Override
    protected Class<V1HorizontalPodAutoscaler> getArtifactType() {
        return V1HorizontalPodAutoscaler.class;
    }

    @Override
    protected void createArtifact(V1HorizontalPodAutoscaler artifact, Map<String, Object> rawArtifact) {
        try {
            String pretty = Introspection.resolve(rawArtifact, Fields.PRETTY);
            String dryRun = Introspection.resolve(rawArtifact, Fields.DRY_RUN);
            String fieldManager = Introspection.resolve(rawArtifact, Fields.FIELD_MANAGER);
            String fieldValidation = Introspection.resolve(rawArtifact, Fields.FIELD_VALIDATION);
            getAutoscalingApi().createNamespacedHorizontalPodAutoscaler(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s horizontal pod autoscaler creation fail", ex);
        }
    }
}
