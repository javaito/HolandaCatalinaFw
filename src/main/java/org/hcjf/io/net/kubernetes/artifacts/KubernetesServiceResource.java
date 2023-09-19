package org.hcjf.io.net.kubernetes.artifacts;

import org.hcjf.errors.HCJFRuntimeException;

import io.kubernetes.client.openapi.models.V1Service;

public class KubernetesServiceResource extends KubernetesArtifactResource<V1Service> {

    public static final String NAME = "system_k8s_service";

    @Override
    public String getImplName() {
        return NAME;
    }
    
    @Override
    protected Class<V1Service> getArtifactType() {
        return V1Service.class;
    }


    @Override
    protected void createArtifact(V1Service artifact, String pretty, String dryRun, String fieldManager,
            String fieldValidation) {
        try {
            getCoreApi().createNamespacedService(getNamespace(), artifact, pretty, dryRun, fieldManager,
                    fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s service creation fail", ex);
        }
    }

    @Override
    protected void updateArtifact(String name, V1Service artifact, String pretty, String dryRun, String fieldManager,
            String fieldValidation) {
        try {
            getCoreApi().replaceNamespacedService(name, getNamespace(), artifact, pretty, dryRun, fieldManager,
                    fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s service creation fail", ex);
        }
    }

    @Override
    protected boolean isCreated(String manifestName) {
        boolean result = false;
        try {
            V1Service service = getCoreApi().readNamespacedService(manifestName, getNamespace(), null);
            if (service != null) {
                result = true;
            }
        } catch (Exception ex) {
        }
        return result;
    }

}
