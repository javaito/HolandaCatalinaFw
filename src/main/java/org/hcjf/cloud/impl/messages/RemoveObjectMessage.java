package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class RemoveObjectMessage extends Message {

    private Object[] path;

    public RemoveObjectMessage() {
    }

    public RemoveObjectMessage(UUID id) {
        super(id);
    }

    public Object[] getPath() {
        return path;
    }

    public void setPath(Object[] path) {
        this.path = path;
    }
}
