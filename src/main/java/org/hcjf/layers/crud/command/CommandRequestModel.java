package org.hcjf.layers.crud.command;

import java.util.Map;

public class CommandRequestModel {

    private String command;
    private Map<String, Object> payload;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
