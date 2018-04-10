package org.hcjf.cloud.impl.objects;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author javaito
 */
public class DistributedLayer {

    private final Class layerInterface;
    private final String layerName;
    private final List<UUID> nodes;
    private final Map<UUID,AtomicLong> nodesInvocationCounter;

    public DistributedLayer(Class layerInterface, String layerName) {
        this.layerInterface = layerInterface;
        this.layerName = layerName;
        this.nodes = new ArrayList<>();
        this.nodesInvocationCounter = new HashMap<>();
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

        //Clean all the counters because there are a new node to resolve the layer invoke.
        for(UUID id : nodesInvocationCounter.keySet()) {
            nodesInvocationCounter.put(id, new AtomicLong());
        }

        nodesInvocationCounter.put(nodeId, new AtomicLong());
    }

    public synchronized void removeNode(UUID nodeId) {
        nodesInvocationCounter.remove(nodeId);
        nodes.remove(nodeId);
    }

    public synchronized void nodeInvoked(UUID nodeId) {
        if(nodesInvocationCounter.containsKey(nodeId)) {
            nodesInvocationCounter.get(nodeId).addAndGet(1);
            nodes.sort((L, R) ->
                    (int) (nodesInvocationCounter.get(L).get() - nodesInvocationCounter.get(R).get()));
        }
    }
}
