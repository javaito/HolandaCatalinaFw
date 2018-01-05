package org.hcjf.cloud.impl.messages;

/**
 * @author javaito
 */
public final class AckMessage extends Message {

    public AckMessage() {}

    public AckMessage(Message message) {
        super(message.getId());
    }

}
