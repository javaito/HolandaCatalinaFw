package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class MethodResponseMessage extends Message {

    private Object response;

    public MethodResponseMessage() {
    }

    public MethodResponseMessage(UUID id) {
        super(id);
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
