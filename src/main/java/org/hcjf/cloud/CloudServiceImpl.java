package org.hcjf.cloud;

import org.hcjf.cloud.cache.CloudCache;
import org.hcjf.cloud.cache.CloudCacheStrategy;
import org.hcjf.cloud.counter.Counter;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * This clas must be implemented in order to create an
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
    public <K extends Object, V extends Object> Map<K, V> getMap(String mapName);

    /**
     * This method provides an implementation of distributed queue. All the nodes
     * on the cluster shares this instance.
     * @param queueName Name of the queue.
     * @param <V> Type of the queue's values.
     * @return Return the instance of the distributed queue.
     */
    public <V extends Object> Queue<V> getQueue(String queueName);

    /**
     * This method provides an implementation of distributed set. All the nodes
     * on the cloud shares this instance.
     * @param setName Name of the set.
     * @param <V> Type of the set's values.
     * @return Return the instance of the distributed set.
     */
    public <V extends Object> Set<V> getSet(String setName);

    /**
     * This method provides an implementation of distributed counter. All the nodes
     * on the cloud shares this instance.
     * @param counterName Name of the counter.
     * @return Return thr instance of the counter.
     */
    public Counter getCounter(String counterName);

    /**
     * This method takes a resource an lock this for all the thread around the cluster
     * and this resource has locked for all the thread for execution.
     * This method is blocked until you can get the lock.
     * @param resourceName The name of the resource to lock.
     */
    public void lock(String resourceName) throws InterruptedException;

    /**
     * This method unlocks a previously locked resource.
     * @param resourceName The name of the resource locked.
     */
    public void unlock(String resourceName) throws InterruptedException;

    /**
     * Return the implementation of the Lock interface distributed.
     * @param lockName Name of the lock.
     * @return Distributed lock implementation.
     */
    public Lock getLock(String lockName);

    /**
     * Return the distributed lock condition over specific lock object.
     * @param conditionName Lock condition name.
     * @param lock Specific lock object.
     * @return Return the lock condition.
     */
    public Condition getCondition(String conditionName, Lock lock);

    /**
     * Creates a instance of cache into the cloud using the specific strategy to
     * specify the behavior of the cache instance.
     * @param cacheName Name of the cache instance.
     * @param strategy Cache strategy.
     */
    public void createCache(String cacheName, CloudCacheStrategy strategy);

    /**
     * Return the instance of cache named with specific name.
     * @param cacheName Name of the instance of cache.
     * @return Instance of cache.
     */
    public CloudCache getCache(String cacheName);

    /**
     * Shutdown hook
     */
    public void shutdown();

}
