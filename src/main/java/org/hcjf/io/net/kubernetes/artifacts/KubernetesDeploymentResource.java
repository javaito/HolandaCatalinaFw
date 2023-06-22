package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V1Deployment;
import org.hcjf.errors.HCJFRuntimeException;
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
    protected void createArtifact(V1Deployment artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {
           getAppsApi().createNamespacedDeployment(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s deployment creation fail", ex);
        }
    }

    @Override
    protected void updateArtifact(String name, V1Deployment artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {
            getAppsApi().replaceNamespacedDeployment(name, getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s deployment creation fail", ex);
        }
    }

    @Override
    protected boolean isCreated(String manifestName) {
        boolean result = false;
        try {
            V1Deployment deployment = getAppsApi().readNamespacedDeployment(manifestName, getNamespace(), null);
            if (deployment != null){
                result = true;
            }
        } catch (Exception ex) {}
        return result;
    }
}
