package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class LockMessage extends Message {

    private Object[] path;
    private Long timestamp;
    private Long nanos;

    public LockMessage() {
    }

    public LockMessage(UUID id) {
        super(id);
    }

    public Object[] getPath() {
        return path;
    }

    public void setPath(Object[] path) {
        this.path = path;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getNanos() {
        return nanos;
    }

    public void setNanos(Long nanos) {
        this.nanos = nanos;
    }
}
