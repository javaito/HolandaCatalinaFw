package org.hcjf.layers.crud.command;

import java.util.Map;

/**
 * Represents a command request. Specifies the command name and its associated payload.
 */
public class CommandRequestModel {

    private String command;
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
