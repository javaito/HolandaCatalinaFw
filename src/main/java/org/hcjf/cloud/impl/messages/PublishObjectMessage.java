package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class PublishObjectMessage extends Message {

    private String path;
    private Long timestamp;
    private Object value;

    public PublishObjectMessage() {
    }

    public PublishObjectMessage(UUID id) {
        super(id);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
