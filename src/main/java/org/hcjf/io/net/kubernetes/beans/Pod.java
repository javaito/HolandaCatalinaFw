package org.hcjf.io.net.kubernetes.beans;

/**
 * @author javaito
 */
public class Pod extends KubernetesBean {

    public static final class Fields {
        public static final String API_VERSION = "apiVersion";
        public static final String METADATA = "metadata";
        public static final String SPEC = "spec";
        public static final String STATUS = "status";
    }

    private String apiVersion;
    private PodMetadata metadata;
    private PodSpec spec;
    private PodStatus status;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public PodMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PodMetadata metadata) {
        this.metadata = metadata;
    }

    public PodSpec getSpec() {
        return spec;
    }

    public void setSpec(PodSpec spec) {
        this.spec = spec;
    }

    public PodStatus getStatus() {
        return status;
    }

    public void setStatus(PodStatus status) {
        this.status = status;
    }
}
