package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class PublishPathMessage extends Message {

    private String[] path;

    public PublishPathMessage() {
    }

    public PublishPathMessage(UUID id) {
        super(id);
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }
}
