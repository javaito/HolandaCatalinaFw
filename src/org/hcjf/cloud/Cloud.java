package org.hcjf.cloud;

import org.hcjf.properties.SystemProperties;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * This class is a singleton that provides the generics
 * cloud interface, the implementation of this interface
 * depends of the system property 'hcjf.cloud.impl'
 * @author javaito
 * @mail javaito@gmail.com
 */
public final class Cloud {

    private static Cloud instance;

    private final CloudServiceImpl impl;

    /**
     * Private constructor
     */
    private Cloud() {
        String implClassName = SystemProperties.get(SystemProperties.CLOUD_IMPL);
        if(implClassName == null) {
            throw new IllegalArgumentException("Implementation cloud class is null, see the system property 'hcjf.cloud.impl'");
        }
        try {
            Class<? extends CloudServiceImpl> implClass =
                    (Class<? extends CloudServiceImpl>)
                            Class.forName(implClassName);
            impl = implClass.newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to create cloud implementation", ex);
        }
    }

    /**
     * Return the instance of the cloud service.
     * @return Instance of the cloud.
     */
    private synchronized static Cloud getInstance() {
        if(instance == null) {
            instance = new Cloud();
        }

        return instance;
    }

    /**
     * This method provides an implementation of distributed map. All the nodes
     * on the cluster shares this instance.
     * @param mapName Name of the map.
     * @param <K> Type of the map's key.
     * @param <V> Type of the map's values.
     * @return Return the instance of the distributed map.
     */
    public static <K extends Object, V extends Object> Map<K, V> getMap(String mapName) {
        return getInstance().impl.getMap(mapName);
    }

    /**
     * This method provides an implementation of distributed queue. All the nodes
     * on the cluster shares this instance.
     * @param queueName Name of the queue.
     * @param <V> Type of the queue's values.
     * @return Return the instance of the distributed queue.
     */
    public static <V extends Object> Queue<V> getQueue(String queueName) {
        return getInstance().impl.getQueue(queueName);
    }

    /**
     * This method takes a resource an lock this for all the thread around the cluster
     * and this resource has locked for all the thread for execution.
     * This method is blocked until you can get the lock.
     * @param resourceName The name of the resource to lock.
     */
    public static void lock(String resourceName) throws InterruptedException {
        getInstance().impl.lock(resourceName);
    }

    /**
     * This method unlocks a previously locked resource.
     * @param resourceName The name of the resource locked.
     */
    public static void unlock(String resourceName) throws InterruptedException {
        getInstance().impl.unlock(resourceName);
    }

    /**
     * Return the implementation of the Lock interface distributed.
     * @param lockName Name of the lock.
     * @return Distributed lock implementation.
     */
    public static Lock getLock(String lockName) {
        return getInstance().impl.getLock(lockName);
    }

    /**
     * Return the distributed lock condition over specific lock object.
     * @param conditionName Lock condition name.
     * @param lock Specific lock object.
     * @return Return the lock condition.
     */
    public static Condition getCondition(String conditionName, Lock lock) {
        return getInstance().impl.getCondition(conditionName, lock);
    }
}
