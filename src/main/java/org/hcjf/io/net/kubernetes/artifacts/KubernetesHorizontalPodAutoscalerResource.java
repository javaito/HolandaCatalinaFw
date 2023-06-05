package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V2beta2HorizontalPodAutoscaler;
import org.hcjf.errors.HCJFRuntimeException;

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
    protected void createArtifact(V2beta2HorizontalPodAutoscaler artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {
            getAutoscalingV2beta2Api().createNamespacedHorizontalPodAutoscaler(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s horizontal pod autoscaler creation fail", ex);
        }
    }

    @Override
    protected void updateArtifact(String name, V2beta2HorizontalPodAutoscaler artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {
            getAutoscalingV2beta2Api().replaceNamespacedHorizontalPodAutoscaler(name,getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s cron job creation fail", ex);
        }
    }

    @Override
    protected boolean isDeployed(String manifestName) {
        boolean result = false;
        try {
            V2beta2HorizontalPodAutoscaler horizontalPodAutoscaler = getAutoscalingV2beta2Api().readNamespacedHorizontalPodAutoscaler(manifestName, getNamespace(), null);
            if (horizontalPodAutoscaler != null){
                result = true;
            }
        } catch (Exception ex) {}
        return result;
    }
}
