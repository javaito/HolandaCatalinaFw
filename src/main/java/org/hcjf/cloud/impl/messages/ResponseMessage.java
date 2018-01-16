package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class ResponseMessage extends Message {

    private Object value;

    public ResponseMessage() {
    }

    public ResponseMessage(UUID id) {
        super(id);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
