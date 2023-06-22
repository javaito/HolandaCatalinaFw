package org.hcjf.io.net.kubernetes.artifacts;

import io.kubernetes.client.openapi.models.V1CronJob;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.Introspection;

import java.util.Map;

public class KubernetesCronJobResource extends KubernetesArtifactResource<V1CronJob> {

    public static final String NAME = "system_k8s_cron_job";

    @Override
    public String getImplName() {
        return NAME;
    }

    @Override
    protected Class<V1CronJob> getArtifactType() {
        return V1CronJob.class;
    }

    @Override
    protected void createArtifact(V1CronJob artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {
            getBatchApi().createNamespacedCronJob(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s cron job creation fail", ex);
        }
    }

    @Override
    protected void updateArtifact(String name, V1CronJob artifact, String pretty, String dryRun, String fieldManager, String fieldValidation) {
        try {
            getBatchApi().replaceNamespacedCronJob(name,getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s cron job creation fail", ex);
        }
    }

    @Override
    protected boolean isCreated(String manifestName) {
        boolean result = false;
        try {
            V1CronJob cronJob = getBatchApi().readNamespacedCronJob(manifestName, getNamespace(), null);
            if (cronJob != null){
                result = true;
            }
        } catch (Exception ex) {}
        return result;
    }
}
