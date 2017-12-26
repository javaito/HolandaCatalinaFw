package org.hcjf.cloud.impl.messages;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.UUID;

/**
 * @author javaito.
 */
public abstract class Message implements BsonParcelable {

    private final UUID id;
    private final Long timestamp;

    public Message(UUID id) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
    }

    public final Long getTimestamp() {
        return timestamp;
    }

    public UUID getId() {
        return id;
    }
}
