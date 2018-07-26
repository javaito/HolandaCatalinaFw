package org.hcjf.cloud.impl.messages;

import org.hcjf.io.net.messages.Message;

import java.util.UUID;

/**
 * @author javaito
 */
public class PublishLayerMessage extends Message {

    private Object[] path;
    private UUID nodeId;
    private UUID serviceEndPointId;

    public PublishLayerMessage() {
    }

    public PublishLayerMessage(UUID id) {
        super(id);
    }

    public Object[] getPath() {
        return path;
    }

    public void setPath(Object[] path) {
        this.path = path;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    public UUID getServiceEndPointId() {
        return serviceEndPointId;
    }

    public void setServiceEndPointId(UUID serviceEndPointId) {
        this.serviceEndPointId = serviceEndPointId;
    }
}
