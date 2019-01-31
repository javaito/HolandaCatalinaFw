package org.hcjf.cloud.impl.objects;

import java.util.*;

/**
 * @author javaito
 */
public class DistributedLayer {

    private final Class layerInterface;
    private final String layerName;
    private final List<UUID> serviceEndPoints;
    private final Map<UUID,ResponseAverage> invocationCounter;

    public DistributedLayer(Class layerInterface, String layerName) {
        this.layerInterface = layerInterface;
        this.layerName = layerName;
        this.serviceEndPoints = new ArrayList<>();
        this.invocationCounter = new HashMap<>();
    }

    public Class getLayerInterface() {
        return layerInterface;
    }

    public String getLayerName() {
        return layerName;
    }

    public synchronized UUID getServiceToInvoke() {
        UUID result = null;
        if(serviceEndPoints.size() > 0) {
            result = serviceEndPoints.get(0);
            serviceEndPoints.add(result);
        }
        return result;
    }

    public synchronized void addServiceEndPoint(UUID serviceEndPointId) {
        serviceEndPoints.add(serviceEndPointId);
        invocationCounter.put(serviceEndPointId, new ResponseAverage());
    }

    public synchronized void removeServiceEndPoint(UUID serviceEndPointId) {
        serviceEndPoints.remove(serviceEndPointId);
        invocationCounter.remove(serviceEndPointId);
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
