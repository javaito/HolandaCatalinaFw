package org.hcjf.cloud.impl;

import org.hcjf.cloud.CloudServiceImpl;
import org.hcjf.cloud.cache.CloudCache;
import org.hcjf.cloud.cache.CloudCacheStrategy;
import org.hcjf.cloud.counter.Counter;
import org.hcjf.cloud.impl.network.CloudOrchestrator;
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

    /**
     * This method provides an implementation of distributed map. All the nodes
     * on the cluster shares this instance.
     * @param mapName Name of the map.
     * @param <K> Type of the map's key.
     * @param <V> Type of the map's values.
     * @return Return the instance of the distributed map.
     */
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

    /**
     * This method provides an implementation of distributed queue. All the nodes
     * on the cluster shares this instance.
     * @param queueName Name of the queue.
     * @param <V> Type of the queue's values.
     * @return Return the instance of the distributed queue.
     */
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

    /**
     * This method provides an implementation of distributed set. All the nodes
     * on the cloud shares this instance.
     * @param setName Name of the set.
     * @param <V> Type of the set's values.
     * @return Return the instance of the distributed set.
     */
    @Override
    public <V> Set<V> getSet(String setName) {
        return null;
    }

    /**
     * This method provides an implementation of distributed counter. All the nodes
     * on the cloud shares this instance.
     * @param counterName Name of the counter.
     * @return Return thr instance of the counter.
     */
    @Override
    public Counter getCounter(String counterName) {
        return null;
    }

    /**
     * This method takes a resource an lock this for all the thread around the cluster
     * and this resource has locked for all the thread for execution.
     * This method is blocked until you can get the lock.
     * @param resourceName The name of the resource to lock.
     * @throws InterruptedException Interrupted exception
     */
    @Override
    public void lock(String resourceName) throws InterruptedException {
        getLock(resourceName).lock();
    }

    /**
     * This method unlocks a previously locked resource.
     * @param resourceName The name of the resource locked.
     * @throws InterruptedException Interrupted exception.
     */
    @Override
    public void unlock(String resourceName) throws InterruptedException {
        getLock(resourceName).unlock();
    }

    /**
     * Return the implementation of the Lock interface distributed.
     * @param lockName Name of the lock.
     * @return Distributed lock implementation.
     */
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

    /**
     * Return the distributed lock condition over specific lock object.
     * @param conditionName Lock condition name.
     * @param lock Specific lock object.
     * @return Return the lock condition.
     */
    @Override
    public Condition getCondition(String conditionName, Lock lock) {
        return ((LockImpl)lock).newCondition(conditionName);
    }

    /**
     * Creates a instance of cache into the cloud using the specific strategy to
     * specify the behavior of the cache instance.
     * @param cacheName Name of the cache instance.
     * @param strategies Set with the strategies for the cache instance.
     */
    @Override
    public void createCache(String cacheName, Set<CloudCacheStrategy> strategies) {

    }

    /**
     * Return the instance of cache named with specific name.
     * @param cacheName Name of the instance of cache.
     * @return Instance of cache.
     */
    @Override
    public CloudCache getCache(String cacheName) {
        return null;
    }

    /**
     * Dispatch the event instance to the cloud.
     * @param event Event instance.
     */
    @Override
    public void dispatchEvent(DistributedEvent event) {
        CloudOrchestrator.getInstance().dispatchEvent(event);
    }

    /**
     * Publish a distributed layer into the cloud.
     * @param layerClass Layer class.
     * @param implName Layer implementation name.
     */
    @Override
    public void publishDistributedLayer(Class<? extends LayerInterface> layerClass, String implName, String regex) {
        CloudOrchestrator.getInstance().publishDistributedLayer(regex, Layer.class.getName(), layerClass.getName(), implName);
    }

    /**
     * This method verifies if the layer and name indicated are published into the cloud.
     * @param layerClass Layer class.
     * @param implName Layer implementation name.
     * @return Returns true if the layer is published and false in the otherwise.
     */
    @Override
    public boolean isLayerPublished(Class<? extends LayerInterface> layerClass, String implName) {
        return CloudOrchestrator.getInstance().isDistributedLayerPublished(Layer.class.getName(), layerClass.getName(), implName);
    }

    /**
     * Returns the object that represent the distributed layer.
     * @param layerClass Layer class.
     * @param implName Layer implementation name.
     * @return Regex if exist or null.
     */
    @Override
    public String getRegexFromDistributedLayer(Class<? extends LayerInterface> layerClass, String implName) {
        return CloudOrchestrator.getInstance().getRegexFromDistributedLayer(Layer.class.getName(), layerClass.getName(), implName);
    }

    /**
     * This method send the plugin for all the nodes into the cloud.
     * @param jarFile Byte array that represents the jar file.
     */
    @Override
    public void publishPlugin(byte[] jarFile) {
        CloudOrchestrator.getInstance().publishPlugin(jarFile);
    }

    /**
     * Invokes the remote instance of a layer.
     * @param layerClass Layer interface class.
     * @param implName Implementation name.
     * @param method Method to invoke.
     * @param parameters Parameters to invoke.
     * @param <O> Expected return data type.
     * @return Invocation result.
     */
    @Override
    public <O> O layerInvoke(Class<? extends LayerInterface> layerClass, String implName, Method method, Object... parameters) {
        return CloudOrchestrator.getInstance().layerInvoke(parameters, method, Layer.class.getName(), layerClass.getName(), implName);
    }

    /**
     * This method must start the process of interaction with other services.
     */
    @Override
    public void publishMe() {
        CloudOrchestrator.getInstance().publishMe();
    }

    /**
     * This method start a worker over the cloud implementation to make a task and finish.
     * @param workerConfig Map with all the parameters to configure a worker instance.
     */
    @Override
    public void forkWorker(Map<String, Object> workerConfig) {

    }

    /**
     * Shutdown hook
     */
    @Override
    public void shutdown() {
    }
}
