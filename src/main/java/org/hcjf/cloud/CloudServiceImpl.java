package org.hcjf.cloud;

import org.hcjf.cloud.cache.CloudCache;
import org.hcjf.cloud.cache.CloudCacheStrategy;
import org.hcjf.cloud.counter.Counter;
import org.hcjf.events.DistributedEvent;
import org.hcjf.layers.LayerInterface;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * This class must be implemented in order to create an
 * implementation to resolve the cloud interface.
 * @author javaito
 */
public interface CloudServiceImpl {

    /**
     * This method provides an implementation of distributed map. All the nodes
     * on the cluster shares this instance.
     * @param mapName Name of the map.
     * @param <K> Type of the map's key.
     * @param <V> Type of the map's values.
     * @return Return the instance of the distributed map.
     */
    <K extends Object, V extends Object> Map<K, V> getMap(String mapName);

    /**
     * This method provides an implementation of distributed queue. All the nodes
     * on the cluster shares this instance.
     * @param queueName Name of the queue.
     * @param <V> Type of the queue's values.
     * @return Return the instance of the distributed queue.
     */
    <V extends Object> Queue<V> getQueue(String queueName);

    /**
     * This method provides an implementation of distributed set. All the nodes
     * on the cloud shares this instance.
     * @param setName Name of the set.
     * @param <V> Type of the set's values.
     * @return Return the instance of the distributed set.
     */
    <V extends Object> Set<V> getSet(String setName);

    /**
     * This method provides an implementation of distributed counter. All the nodes
     * on the cloud shares this instance.
     * @param counterName Name of the counter.
     * @return Return thr instance of the counter.
     */
    Counter getCounter(String counterName);

    /**
     * This method takes a resource an lock this for all the thread around the cluster
     * and this resource has locked for all the thread for execution.
     * This method is blocked until you can get the lock.
     * @param resourceName The name of the resource to lock.
     * @throws InterruptedException Interrupted exception
     */
    void lock(String resourceName) throws InterruptedException;

    /**
     * This method unlocks a previously locked resource.
     * @param resourceName The name of the resource locked.
     * @throws InterruptedException Interrupted exception.
     */
    void unlock(String resourceName) throws InterruptedException;

    /**
     * Return the implementation of the Lock interface distributed.
     * @param lockName Name of the lock.
     * @return Distributed lock implementation.
     */
    Lock getLock(String lockName);

    /**
     * Return the distributed lock condition over specific lock object.
     * @param conditionName Lock condition name.
     * @param lock Specific lock object.
     * @return Return the lock condition.
     */
    Condition getCondition(String conditionName, Lock lock);

    /**
     * Creates a instance of cache into the cloud using the specific strategy to
     * specify the behavior of the cache instance.
     * @param cacheName Name of the cache instance.
     * @param strategies Set with the strategies for the cache instance.
     */
    void createCache(String cacheName, Set<CloudCacheStrategy> strategies);

    /**
     * Return the instance of cache named with specific name.
     * @param cacheName Name of the instance of cache.
     * @return Instance of cache.
     */
    CloudCache getCache(String cacheName);

    /**
     * Dispatch the event instance to the cloud.
     * @param event Event instance.
     */
    void dispatchEvent(DistributedEvent event);

    /**
     * Publish a distributed layer into the cloud.
     * @param layerClass Layer class.
     * @param implName Layer implementation name.
     */
    void publishDistributedLayer(Class<? extends LayerInterface> layerClass, String implName);

    /**
     * This method send the plugin for all the nodes into the cloud.
     * @param jarFile Byte array that represents the jar file.
     */
    void publishPlugin(byte[] jarFile);

    /**
     * This method verifies if the layer and name indicated are published into the cloud.
     * @param layerClass Layer class.
     * @param implName Layer implementation name.
     * @return Returns true if the layer is published and false in the otherwise.
     */
    boolean isLayerPublished(Class<? extends LayerInterface> layerClass, String implName);

    /**
     * Invokes the remote instance of a layer.
     * @param layerClass Layer interface class.
     * @param implName Implementation name.
     * @param method Method to invoke.
     * @param parameters Parameters to invoke.
     * @param <O> Expected return data type.
     * @return Invocation result.
     */
    <O extends Object> O layerInvoke(Class<? extends LayerInterface> layerClass, String implName, Method method, Object... parameters);

    /**
     * Shutdown hook
     */
    void shutdown();

}
