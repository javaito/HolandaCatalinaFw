package org.hcjf.cloud.impl.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author javaito
 */
public class DistributedLayer {

    private final Class layerInterface;
    private final String layerName;
    private final List<UUID> nodes;

    public DistributedLayer(Class layerInterface, String layerName) {
        this.layerInterface = layerInterface;
        this.layerName = layerName;
        this.nodes = new ArrayList<>();
    }

    public Class getLayerInterface() {
        return layerInterface;
    }

    public String getLayerName() {
        return layerName;
    }

    public synchronized UUID getNodeToInvoke() {
        UUID result = null;
        if(nodes.size() > 0) {
            result = nodes.remove(0);
            nodes.add(result);
        }
        return result;
    }

    public synchronized void addNode(UUID nodeId) {
        nodes.add(nodeId);
    }

    public synchronized void removeNode(UUID nodeId) {
        nodes.remove(nodeId);
    }
}
