package org.hcjf.cloud;

import org.hcjf.cloud.cache.CloudCache;
import org.hcjf.cloud.cache.CloudCacheStrategy;
import org.hcjf.cloud.counter.Counter;
import org.hcjf.cloud.timer.CloudTimerTask;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * This class is a singleton that provides the generics
 * cloud interface, the implementation of this interface
 * depends of the system property 'hcjf.cloud.impl'
 * @author javaito
 */
public final class Cloud extends Service<CloudConsumer> {

    private static Cloud instance;

    private final CloudServiceImpl impl;
    private final Timer timer;

    /**
     * Private constructor
     */
    private Cloud() {
        super(SystemProperties.get(SystemProperties.Cloud.SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.Cloud.SERVICE_PRIORITY));
        String implClassName = SystemProperties.get(SystemProperties.Cloud.IMPL);
        if(implClassName == null) {
            throw new IllegalArgumentException("Implementation cloud class is null, see the system property 'hcjf.cloud.impl'");
        }
        try {
            Class<? extends CloudServiceImpl> implClass =
                    (Class<? extends CloudServiceImpl>)
                            Class.forName(implClassName);
            impl = implClass.getConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to create cloud implementation", ex);
        }
        timer = new Timer();
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
     * This method provides an implementation of distributed set. All the nodes
     * on the cloud shares this instance.
     * @param setName Name of the set.
     * @param <V> Type of the set's values.
     * @return Return the instance of the distributed set.
     */
    public static <V extends Object> Set<V> getSet(String setName) {
        return getInstance().impl.getSet(setName);
    }

    /**
     * This method provides an implementation of distributed counter. All the nodes
     * on the cloud shares this instance.
     * @param counterName Name of the counter.
     * @return Return thr instance of the counter.
     */
    public static Counter getCounter(String counterName) {
        return getInstance().impl.getCounter(counterName);
    }

    /**
     * This method takes a resource an lock this for all the thread around the cluster
     * and this resource has locked for all the thread for execution.
     * This method is blocked until you can get the lock.
     * @param resourceName The name of the resource to lock.
     * @throws InterruptedException Interrupted exception
     */
    public static void lock(String resourceName) throws InterruptedException {
        getInstance().impl.lock(resourceName);
    }

    /**
     * This method unlocks a previously locked resource.
     * @param resourceName The name of the resource locked.
     * @throws InterruptedException Interrupted exception
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

    /**
     * Creates a instance of cache into the cloud using the specific strategy to
     * specify the behavior of the cache instance.
     * @param cacheName Name of the cache instance.
     * @param strategies Set with the strategies for the cache instance.
     */
    public static void createCache(String cacheName, Set<CloudCacheStrategy> strategies) {
        getInstance().impl.createCache(cacheName, strategies);
    }

    /**
     * Return the instance of cache named with specific name.
     * @param cacheName Name of the instance of cache.
     * @return Instance of cache.
     */
    public static CloudCache getCache(String cacheName) {
        return getInstance().impl.getCache(cacheName);
    }

    /**
     * Schedule into the cloud service an instance of timer task.
     * @param timerTask Timer task instance.
     */
    public static void createTimerTask(CloudTimerTask timerTask) {
        getInstance().fork(timerTask);
    }

    @Override
    protected void shutdown(ShutdownStage stage) {
        impl.shutdown();
    }

    @Override
    public void registerConsumer(CloudConsumer consumer) {

    }

    @Override
    public void unregisterConsumer(CloudConsumer consumer) {

    }
}
