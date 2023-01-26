package org.hcjf.layers.crud.command;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.Map;

/**
 * Represents a command request. Specifies the command name and its associated payload.
 */
public class CommandRequestModel implements BsonParcelable {

    private String command;
    private Object instanceId;
    private Map<String, Object> payload;

    /**
     * The command name
     * @return the command name
     */
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Object getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Object instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * The command's associated payload
     * @return the command's associated payload
     */
    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
