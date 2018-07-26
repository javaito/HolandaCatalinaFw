package org.hcjf.cloud.impl.messages;

import org.hcjf.cloud.impl.network.Node;
import org.hcjf.io.net.messages.Message;

import java.util.UUID;

/**
 * @author javaito
 */
public class NodeIdentificationMessage extends Message {

    private Node node;

    public NodeIdentificationMessage() {
    }

    public NodeIdentificationMessage(Node node) {
        super(UUID.randomUUID());
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
