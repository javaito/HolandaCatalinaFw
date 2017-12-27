package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class KeepAliveMessage extends Message {

    public KeepAliveMessage() {
        super(UUID.randomUUID());
    }

}
