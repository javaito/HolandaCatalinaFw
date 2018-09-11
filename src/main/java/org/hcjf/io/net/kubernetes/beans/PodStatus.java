package org.hcjf.io.net.kubernetes.beans;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.List;

/**
 * @author javaito
 */
public class PodStatus implements BsonParcelable {

    public static final class Fields {
        public static final String PHASE = "phase";
        public static final String HOST_IP = "hostIP";
        public static final String POD_IP = "podIP";
        public static final String START_TIME = "startTime";
        public static final String QOS_CLASS = "qosClass";
        public static final String CONDITIONS = "conditions";
    }

    private String phase;
    private String hostIp;
    private String podIp;
    private Long startTime;
    private String qosClass;
    private List<PodStatusCondition> conditions;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getPodIp() {
        return podIp;
    }

    public void setPodIp(String podIp) {
        this.podIp = podIp;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getQosClass() {
        return qosClass;
    }

    public void setQosClass(String qosClass) {
        this.qosClass = qosClass;
    }

    public List<PodStatusCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<PodStatusCondition> conditions) {
        this.conditions = conditions;
    }
}
