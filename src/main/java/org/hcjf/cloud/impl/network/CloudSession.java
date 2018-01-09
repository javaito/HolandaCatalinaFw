package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.impl.messages.AckMessage;
import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;

import java.util.UUID;

/**
 * @author javaito.
 */
public class CloudSession extends NetSession {

    private AckMessage ackMessage;

    public CloudSession(NetServiceConsumer consumer) {
        super(UUID.randomUUID(), consumer);
    }

    public AckMessage getAckMessage() {
        return ackMessage;
    }

    public void setAckMessage(AckMessage ackMessage) {
        this.ackMessage = ackMessage;
    }
}
