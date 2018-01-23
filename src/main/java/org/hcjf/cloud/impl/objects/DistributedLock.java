package org.hcjf.cloud.impl.objects;

/**
 * @author javaito
 */
public class DistributedLock{

    private Long timestamp;
    private Status status;

    public DistributedLock() {
        status = Status.UNLOCKED;
        this.timestamp = Long.MAX_VALUE;
    }

    public final Status getStatus() {
        return status;
    }

    public final void setStatus(Status status) {
        this.timestamp = System.currentTimeMillis();
        this.status = status;
    }

    public final Long getTimestamp() {
        return timestamp;
    }

    public enum Status {

        UNLOCKED,

        LOCKING,

        LOCKED,

        WAITING

    }
}
