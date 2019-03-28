package org.hcjf.io.net.kubernetes;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.BatchV1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Job;
import io.kubernetes.client.models.V1JobSpec;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.util.Config;
import org.hcjf.layers.Layer;
import org.hcjf.layers.crud.CreateLayerInterface;
import org.hcjf.utils.Introspection;

import java.io.IOException;
import java.util.Map;

public class KubernetesJobResource extends Layer implements CreateLayerInterface<Map<String,Object>> {

    private static final class Fields {
        private static final String METADATA_NAME = "metadata.name";
        private static final String IMAGE = "image";
        private static final String COMMAND = "command";
        private static final String METADATA_SPEC_RESTART_POLICY = "metadata.spec.restartPolicy";
        private static final String METADATA_SPEC_BACKOFF_LIMIT = "metadata.spec.backoffLimit";
        private static final String METADATA_SPEC_ACTIVE_DEAD_LINE_SECONDS = "metadata.spec.activeDeadlineSeconds";
        private static final String METADATA_SPEC_COMPLETITIONS = "metadata.spec.completions";
    }

    private static final class Defaults {
        private static final String API_VERSION = "batch/v1";
        private static final String KIND = "Job";
    }

    public static final String NAME = "system_k8s_job";

    private final ApiClient client;
    private final CoreV1Api coreApi;
    private final BatchV1Api batchApi;

    public KubernetesJobResource() {
        try {
            client = Config.fromCluster();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        Configuration.setDefaultApiClient(client);
        this.coreApi = new CoreV1Api();
        this.batchApi = new BatchV1Api();
    }

    @Override
    public String getImplName() {
        return NAME;
    }

    @Override
    public Map<String, Object> create(Map<String, Object> job) {

        if(Introspection.resolve(job, Fields.METADATA_NAME) != null) {
            throw new RuntimeException("The job instance must contains a 'metadata.name' path");
        }

        V1Job k8sJob = new V1Job();
        k8sJob.setApiVersion(Defaults.API_VERSION);
        k8sJob.setKind(Defaults.KIND);

        V1ObjectMeta k8sMetaData = new V1ObjectMeta();
        k8sMetaData.setName(Introspection.resolve(job, Fields.METADATA_NAME));
        k8sJob.setMetadata(k8sMetaData);

        V1JobSpec k8sJobSpec = new V1JobSpec();


        return null;
    }
}
