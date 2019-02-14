package org.hcjf.cloud.impl;

import org.hcjf.cloud.CloudServiceImpl;
import org.hcjf.cloud.cache.CloudCache;
import org.hcjf.cloud.cache.CloudCacheStrategy;
import org.hcjf.cloud.counter.Counter;
import org.hcjf.cloud.impl.network.CloudOrchestrator;
import org.hcjf.cloud.timer.CloudTimerTask;
import org.hcjf.events.DistributedEvent;
import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author javaito
 */
public class DefaultCloudServiceImpl implements CloudServiceImpl {

    private final Map<String,Map> mapInstances;
    private final Map<String,Queue> queueInstances;
    private final Map<String,Lock> lockInstances;

    public DefaultCloudServiceImpl() {
        this.mapInstances = new HashMap<>();
        this.queueInstances = new HashMap<>();
        this.lockInstances = new HashMap<>();
    }

    @Override
    public <K, V> Map<K, V> getMap(String mapName) {
        Map<K, V> result;
        synchronized (mapInstances) {
            result = mapInstances.get(mapName);
            if(result == null) {
                result = new MapImpl<>(mapName);
                mapInstances.put(mapName, result);
            }
        }
        return result;
    }

    @Override
    public <V> Queue<V> getQueue(String queueName) {
        Queue<V> result;
        synchronized (queueInstances) {
            result = queueInstances.get(queueName);
            if(result == null) {
                result = new QueueImpl<>(queueName);
                queueInstances.put(queueName, result);
            }
        }
        return result;
    }

    @Override
    public <V> Set<V> getSet(String setName) {
        return null;
    }

    @Override
    public Counter getCounter(String counterName) {
        return null;
    }

    @Override
    public void lock(String resourceName) throws InterruptedException {
        getLock(resourceName).lock();
    }

    @Override
    public void unlock(String resourceName) throws InterruptedException {
        getLock(resourceName).unlock();
    }

    @Override
    public Lock getLock(String lockName) {
        Lock result;
        synchronized (lockInstances) {
            result = lockInstances.get(lockName);
            if(result == null) {
                result = new LockImpl(lockName);
                lockInstances.put(lockName, result);
            }
        }
        return result;
    }

    @Override
    public Condition getCondition(String conditionName, Lock lock) {
        return ((LockImpl)lock).newCondition(conditionName);
    }

    @Override
    public void createCache(String cacheName, Set<CloudCacheStrategy> strategies) {

    }

    @Override
    public CloudCache getCache(String cacheName) {
        return null;
    }

    @Override
    public void dispatchEvent(DistributedEvent event) {
        CloudOrchestrator.getInstance().dispatchEvent(event);
    }

    @Override
    public void publishDistributedLayer(Class<? extends LayerInterface> layerClass, String implName) {
        CloudOrchestrator.getInstance().publishDistributedLayer(Layer.class.getName(), layerClass.getName(), implName);
    }

    @Override
    public boolean isLayerPublished(Class<? extends LayerInterface> layerClass, String implName) {
        return CloudOrchestrator.getInstance().isDistributedLayerPublished(Layer.class.getName(), layerClass.getName(), implName);
    }

    @Override
    public void publishPlugin(byte[] jarFile) {
        CloudOrchestrator.getInstance().publishPlugin(jarFile);
    }

    @Override
    public <O> O layerInvoke(Class<? extends LayerInterface> layerClass, String implName, Method method, Object... parameters) {
        return CloudOrchestrator.getInstance().layerInvoke(parameters, method, Layer.class.getName(), layerClass.getName(), implName);
    }

    @Override
    public void publishMe() {
        CloudOrchestrator.getInstance().publishMe();
    }

    @Override
    public void shutdown() {
    }
}
