package org.hcjf.cloud.impl.objects;

import java.util.*;

/**
 * @author javaito
 */
public class DistributedLayer {

    private final Class layerInterface;
    private final String layerName;
    private final List<UUID> nodes;
    private final Map<UUID,ResponseAverage> nodesInvocationCounter;

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
        nodesInvocationCounter.put(nodeId, new ResponseAverage());
    }

    public synchronized void removeNode(UUID nodeId) {
        nodes.remove(nodeId);
        nodesInvocationCounter.remove(nodeId);
    }

    public synchronized void addResponseTime(UUID nodeId, Long responseTime) {
        if(nodesInvocationCounter.containsKey(nodeId)) {
            nodesInvocationCounter.get(nodeId).add(responseTime);
            nodes.sort((L, R) ->
                    (int) (nodesInvocationCounter.get(L).get() - nodesInvocationCounter.get(R).get()));
        }
    }

    private class ResponseAverage {

        private long accumulator;
        private int size;

        public ResponseAverage() {
            accumulator = 0;
            size = 1;
        }

        public void add(long value) {
            if(size == 100) {
                accumulator = accumulator / size;
                size = 1;
            }
            accumulator += value;
            size++;
        }

        public long get() {
            return accumulator / size;
        }
    }
}
