package org.hcjf.io.net.messages;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.Map;
import java.util.UUID;

/**
 * @author javaito.
 */
public abstract class Message implements BsonParcelable {

    private UUID id;
    private Long timestamp;
    private UUID sessionId;
    private Map<String,Object> sessionBean;

    public Message() {
    }

    public Message(UUID id) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Returns the id of the message.
     * @return Id of the message.
     */
    public final UUID getId() {
        return id;
    }

    /**
     * Set the id of the message.
     * @param id Id of the message.
     */
    public final void setId(UUID id) {
        this.id = id;
    }

    /**
     * Returns the timestamp of the message.
     * @return Timestamp of the message.
     */
    public final Long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp of the message.
     * @param timestamp Timestamp of the message.
     */
    public final void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the session id for the message.
     * @return Session id.
     */
    public final UUID getSessionId() {
        return sessionId;
    }

    /**
     * Set the session id for the message.
     * @param sessionId Session id.
     */
    public final void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Returns the serialized session instance.
     * @return Serialized session instance.
     */
    public Map<String, Object> getSessionBean() {
        return sessionBean;
    }

    /**
     * Set the serialized session instance.
     * @param sessionBean Serialized session instance.
     */
    public void setSessionBean(Map<String, Object> sessionBean) {
        this.sessionBean = sessionBean;
    }
}
