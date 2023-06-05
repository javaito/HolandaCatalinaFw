package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V1Job;
import org.hcjf.errors.HCJFRuntimeException;

import java.util.Map;

public class KubernetesJobResource extends KubernetesArtifactResource<V1Job> {

    public static final String NAME = "system_k8s_job";

    @Override
    public String getImplName() {
        return NAME;
    }

    @Override
    protected Class<V1Job> getArtifactType() {
        return V1Job.class;
    }

    @Override
    protected void createArtifact(V1Job artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {
            getBatchApi().createNamespacedJob(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s job creation fail", ex);
        }
    }

    @Override
    protected void updateArtifact(String name, V1Job artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {
            getBatchApi().replaceNamespacedJob(name, getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s job creation fail", ex);
        }
    }

    @Override
    protected boolean isDeployed(String manifestName) {
        boolean result = false;
        try {
            V1Job job = getBatchApi().readNamespacedJob(manifestName, getNamespace(), null);
            if (job != null){
                result = true;
            }
        } catch (Exception ex) {}
        return result;
    }
}
