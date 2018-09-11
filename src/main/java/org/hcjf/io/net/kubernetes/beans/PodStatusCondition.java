package org.hcjf.io.net.kubernetes.beans;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.Date;

/**
 * @author javaito
 */
public class PodStatusCondition implements BsonParcelable {

    private String type;
    private Boolean status;
    private Date lastProbetime;
    private Date lastTransitionTime;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Date getLastProbetime() {
        return lastProbetime;
    }

    public void setLastProbetime(Date lastProbetime) {
        this.lastProbetime = lastProbetime;
    }

    public Date getLastTransitionTime() {
        return lastTransitionTime;
    }

    public void setLastTransitionTime(Date lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
    }
}
