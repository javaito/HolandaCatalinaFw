package org.hcjf.cloud.impl.messages;

import org.hcjf.io.net.messages.Message;

import java.util.UUID;

/**
 * @author javaito
 */
public class KeepAliveMessage extends Message {

    public KeepAliveMessage() {
        super(UUID.randomUUID());
    }

}
