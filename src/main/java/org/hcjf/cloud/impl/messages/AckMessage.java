package org.hcjf.cloud.impl.messages;

import org.hcjf.io.net.messages.Message;

/**
 * @author javaito
 */
public final class AckMessage extends Message {

    public AckMessage() {}

    public AckMessage(Message message) {
        super(message.getId());
    }

}
