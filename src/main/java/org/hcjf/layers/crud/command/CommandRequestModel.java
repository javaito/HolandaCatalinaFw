package org.hcjf.layers.crud.command;

public class CommandRequestModel {

    private String command;
    private Object payload;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
