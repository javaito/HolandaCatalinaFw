package org.hcjf.cloud.impl.messages;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.UUID;

/**
 * @author javaito.
 */
public abstract class Message implements BsonParcelable {

    private UUID id;
    private Long timestamp;

    public Message() {
    }

    public Message(UUID id) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
