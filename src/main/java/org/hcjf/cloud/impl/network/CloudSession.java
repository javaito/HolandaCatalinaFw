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
    private Node node;

    public CloudSession(NetServiceConsumer consumer) {
        super(UUID.randomUUID(), consumer);
    }

    protected CloudSession(CloudSession cloudSession) {
        super(cloudSession);
        this.ackMessage = cloudSession.ackMessage;
        this.node = cloudSession.node;
    }

    public AckMessage getAckMessage() {
        return ackMessage;
    }

    public void setAckMessage(AckMessage ackMessage) {
        this.ackMessage = ackMessage;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
