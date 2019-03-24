package org.hcjf.cloud;

import org.hcjf.cloud.cache.CloudCache;
import org.hcjf.cloud.cache.CloudCacheStrategy;
import org.hcjf.cloud.counter.Counter;
import org.hcjf.cloud.timer.CloudTimerTask;
import org.hcjf.events.DistributedEvent;
import org.hcjf.layers.LayerInterface;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;

import java.lang.reflect.Method;
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
     * Dispatch the event instance to the cloud.
     * @param event Event instance.
     */
    public static void dispatchEvent(DistributedEvent event) {
        getInstance().impl.dispatchEvent(event);
    }

    /**
     * Schedule into the cloud service an instance of timer task.
     * @param timerTask Timer task instance.
     */
    public static void createTimerTask(CloudTimerTask timerTask) {
        getInstance().fork(timerTask);
    }

    /**
     * Publish a distributed layer into the cloud.
     * @param layerClass Layer class.
     * @param implName Layer implementation name.
     */
    public static void publishDistributedLayer(Class<? extends LayerInterface> layerClass, String implName) {
        getInstance().impl.publishDistributedLayer(layerClass, implName);
    }

    /**
     * This method send the plugin for all the nodes into the cloud.
     * @param jarFile Byte array that represents the jar file.
     */
    public static void publishPlugin(byte[] jarFile) {
        getInstance().impl.publishPlugin(jarFile);
    }

    /**
     * This method verifies if the layer and name indicated are published into the cloud.
     * @param layerClass Layer class.
     * @param implName Layer implementation name.
     * @return Returns true if the layer is published and false in the otherwise.
     */
    public static boolean isLayerPublished(Class<? extends LayerInterface> layerClass, String implName) {
        return getInstance().impl.isLayerPublished(layerClass, implName);
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
    public static <O extends Object> O layerInvoke(Class<? extends LayerInterface> layerClass, String implName, Method method, Object... parameters){
        return getInstance().impl.layerInvoke(layerClass, implName, method, parameters);
    }

    /**
     * This method must start the process of interaction with other services.
     */
    public static void publishMe() {
        getInstance().impl.publishMe();
    }

    /**
     * This method start a worker over the cloud implementation to make a task and finish.
     * @param workerConfig Map with all the parameters to configure a worker instance.
     */
    public static void forkWorker(Map<String,Object> workerConfig) {
        getInstance().impl.forkWorker(workerConfig);
    }

    /**
     * This method is listening the shutdown signal and must start the shutdown process of the cloud implementation.
     * @param stage Shutdown stage.
     */
    @Override
    protected void shutdown(ShutdownStage stage) {
        impl.shutdown();
    }

    /**
     * Register some consumer to use the cloud service.
     * @param consumer Object with the logic to consume the service.
     */
    @Override
    public void registerConsumer(CloudConsumer consumer) { }

    /**
     * Unregister consumer.
     * @param consumer Consumer to unregister.
     */
    @Override
    public void unregisterConsumer(CloudConsumer consumer) { }
}
