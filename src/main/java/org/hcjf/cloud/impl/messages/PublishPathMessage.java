package org.hcjf.cloud.impl.messages;

import org.hcjf.io.net.messages.Message;

import java.util.UUID;

/**
 * @author javaito
 */
public class PublishPathMessage extends Message {

    private Object[] path;

    public PublishPathMessage() {
    }

    public PublishPathMessage(UUID id) {
        super(id);
    }

    public Object[] getPath() {
        return path;
    }

    public void setPath(Object[] path) {
        this.path = path;
    }
}
