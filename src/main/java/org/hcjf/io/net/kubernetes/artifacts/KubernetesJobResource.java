package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V1Job;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.Introspection;

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
    protected void createArtifact(V1Job artifact, Map<String, Object> rawArtifact) {
        try {
            String pretty = Introspection.resolve(rawArtifact, Fields.PRETTY);
            String dryRun = Introspection.resolve(rawArtifact, Fields.DRY_RUN);
            String fieldManager = Introspection.resolve(rawArtifact, Fields.FIELD_MANAGER);
            String fieldValidation = Introspection.resolve(rawArtifact, Fields.FIELD_VALIDATION);
            getBatchApi().createNamespacedJob(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s job creation fail", ex);
        }
    }
}
