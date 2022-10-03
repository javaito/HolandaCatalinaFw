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
    protected void createArtifact(V1CronJob artifact, Map<String, Object> rawArtifact) {
        try {
            String pretty = Introspection.resolve(rawArtifact, Fields.PRETTY);
            String dryRun = Introspection.resolve(rawArtifact, Fields.DRY_RUN);
            String fieldManager = Introspection.resolve(rawArtifact, Fields.FIELD_MANAGER);
            String fieldValidation = Introspection.resolve(rawArtifact, Fields.FIELD_VALIDATION);
            getBatchApi().createNamespacedCronJob(getNamespace(), artifact, pretty, dryRun, fieldManager, fieldValidation);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("K8s cron job creation fail", ex);
        }
    }
}
