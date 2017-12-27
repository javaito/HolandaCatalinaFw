package org.hcjf.cloud.impl.messages;

import java.util.UUID;

/**
 * @author javaito
 */
public class ShutdownMessage extends Message {

    public ShutdownMessage() {
        super(UUID.randomUUID());
    }

}
