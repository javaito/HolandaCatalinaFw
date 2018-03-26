package org.hcjf.cloud.impl.messages;

import org.hcjf.cloud.impl.network.Node;

import java.util.*;

/**
 * @author javaito
 */
public class CloudWagonMessage extends Message {

    private List<Node> nodes;
    private Map<String,List<Message>> messages;

    public CloudWagonMessage() {
        messages = new HashMap<>();
    }

    public CloudWagonMessage(UUID id) {
        super(id);
        messages = new HashMap<>();
        nodes = new ArrayList<>();
    }

    public Map<String,List<Message>> getMessages() {
        return messages;
    }

    public void setMessages(Map<String,List<Message>> messages) {
        this.messages = messages;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
