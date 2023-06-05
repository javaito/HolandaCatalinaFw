package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1CronJob;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.io.net.kubernetes.KubernetesSpy;
import org.hcjf.io.net.kubernetes.KubernetesSpyResource;
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
    protected void createArtifact(V1ConfigMap artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {

            getCoreApi().createNamespacedConfigMap(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s config map creation fail", ex);
        }
    }


    @Override
    protected void updateArtifact(String name, V1ConfigMap artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {
            getCoreApi().replaceNamespacedConfigMap(name, getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s config map creation fail", ex);
        }
    }

    @Override
    protected boolean isDeployed(String manifestName) {
        boolean result = false;
        try {
            V1ConfigMap configMap = getCoreApi().readNamespacedConfigMap(manifestName, getNamespace(), null);
            if (configMap != null){
                result = true;
            }
        } catch (Exception ex) {}
        return result;
    }
}
