package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class GetMessage extends Message {

    private String[] path;

    public GetMessage() {
    }

    public GetMessage(UUID id) {
        super(id);
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }
}
