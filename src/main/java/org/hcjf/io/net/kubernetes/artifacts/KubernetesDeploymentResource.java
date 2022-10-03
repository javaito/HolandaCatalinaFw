package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V1Deployment;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.Introspection;

import java.util.Map;

public class KubernetesDeploymentResource extends KubernetesArtifactResource<V1Deployment> {

    public static final String NAME = "system_k8s_deployment";

    @Override
    public String getImplName() {
        return NAME;
    }

    @Override
    protected Class<V1Deployment> getArtifactType() {
        return V1Deployment.class;
    }

    @Override
    protected void createArtifact(V1Deployment artifact, Map<String,Object> rawArtifact) {
        try {
            String pretty = Introspection.resolve(rawArtifact, Fields.PRETTY);
            String dryRun = Introspection.resolve(rawArtifact, Fields.DRY_RUN);
            String fieldManager = Introspection.resolve(rawArtifact, Fields.FIELD_MANAGER);
            String fieldValidation = Introspection.resolve(rawArtifact, Fields.FIELD_VALIDATION);
            getAppsApi().createNamespacedDeployment(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s deployment creation fail", ex);
        }
    }
}
