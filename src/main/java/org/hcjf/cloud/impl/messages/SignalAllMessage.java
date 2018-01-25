package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class SignalAllMessage extends Message {

    private String lockName;
    private String conditionName;

    public SignalAllMessage() {
    }

    public SignalAllMessage(UUID id) {
        super(id);
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }
}
