package org.hcjf.io.console;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.UUID;

/**
 * Session metadata model.
 * @author javaito
 */
public class SessionMetadata implements BsonParcelable {

    private UUID id;
    private String sessionName;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
}
