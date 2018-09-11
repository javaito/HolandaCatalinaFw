package org.hcjf.io.net.kubernetes.beans;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.List;

/**
 * @author javaito
 */
public class PodSpec implements BsonParcelable {

    public static final class Fields {
        public static final String RESTART_POLICY = "restartPolicy";
        public static final String TERMINATION_GRACE_PERIOD_SECONDS = "terminationGracePeriodSeconds";
        public static final String DNS_POLICY = "dnsPolicy";
        public static final String SERVICE_ACCOUNT_NAME = "serviceAccountName";
        public static final String SERVICE_ACCOUNT = "serviceAccount";
        public static final String NODE_NAME = "nodeName";
        public static final String SCHEDULER_NAME = "schedulerName";
    }

    private String restartPolicy;
    private Integer terminationGracePeriodSeconds;
    private String dnsPolicy;
    private String serviceAccountName;
    private String serviceAccount;
    private String nodeName;
    private String schedulerName;
    private List<PodSpecContainer> containers;

    public String getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public Integer getTerminationGracePeriodSeconds() {
        return terminationGracePeriodSeconds;
    }

    public void setTerminationGracePeriodSeconds(Integer terminationGracePeriodSeconds) {
        this.terminationGracePeriodSeconds = terminationGracePeriodSeconds;
    }

    public String getDnsPolicy() {
        return dnsPolicy;
    }

    public void setDnsPolicy(String dnsPolicy) {
        this.dnsPolicy = dnsPolicy;
    }

    public String getServiceAccountName() {
        return serviceAccountName;
    }

    public void setServiceAccountName(String serviceAccountName) {
        this.serviceAccountName = serviceAccountName;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }

    public void setServiceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public List<PodSpecContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<PodSpecContainer> containers) {
        this.containers = containers;
    }
}
