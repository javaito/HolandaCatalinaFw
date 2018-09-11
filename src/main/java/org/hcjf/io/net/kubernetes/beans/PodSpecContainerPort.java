package org.hcjf.io.net.kubernetes.beans;

/**
 * @author javaito
 */
public class PodSpecContainerPort extends KubernetesBean {

    private static final class Fields {
        public static final String PROTOCOL = "protocol";
        public static final String CONTAINER_PORT = "containerPort";
    }

    private String protocol;
    private Integer containerPort;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }
}
