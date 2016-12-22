package org.hcjf.cloud;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

/**
 * This clas must be implemented in order to create an
 * implementation to resolve the cloud interface.
 * @author javaito
 * @mail javaito@gmail.com
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

}
