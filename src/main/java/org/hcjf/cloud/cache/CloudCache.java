package org.hcjf.cloud.cache;

import org.hcjf.cloud.Cloud;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * This interface provides the method to use a simple cache.
 * @author javaito
 */
public abstract class CloudCache {

    private final String cacheName;
    private final Map<Object, Map<String,Object>> instances;
    private final Set<CloudCacheStrategy> strategies;
    private final Lock lock;
    private final Condition condition;

    protected CloudCache(String cacheName, Set<CloudCacheStrategy> strategies) {
        this.cacheName = cacheName;
        this.strategies = strategies;

        this.instances = Cloud.getMap(
                SystemProperties.get(SystemProperties.Cloud.Cache.MAP_SUFFIX_NAME) + cacheName);
        this.lock = Cloud.getLock(
                SystemProperties.get(SystemProperties.Cloud.Cache.LOCK_SUFFIX_NAME) + cacheName);
        this.condition = Cloud.getCondition(
                SystemProperties.get(SystemProperties.Cloud.Cache.CONDITION_SUFFIX_NAME) + cacheName, lock);
    }

    /**
     * Remove the information associated to the specific id from the cache.
     * @param id Information index.
     */
    public final void remove(Object id) {
        if(instances.containsKey(id)) {
            lock.lock();
            try {
                instances.remove(id);

                for (CloudCacheStrategy strategy : strategies) {
                    strategy.onRemove(id);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Add some information indexed by specific id onto the cloud cache implementation.
     * @param id Information index.
     * @param value Information to put into cache.
     */
    public final void add(Object id, Object value){
        lock.lock();
        try {
            instances.put(id, Introspection.toMap(value));

            Object toRemove;
            for (CloudCacheStrategy strategy : strategies) {
                toRemove = strategy.onAdd(id, value);
                if (toRemove != null) {
                    instances.remove(toRemove);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Return the information indexed by the specific id.
     * @param id Information index.
     * @param resultType Result type.
     * @param <O> Expected kind of information.
     * @return Return the information associated to the specific id.
     */
    public final <O extends Object> O get(Object id, Class<? extends O> resultType) {
        O result = null;
        Map<String,Object> instance = instances.get(id);
        if(instance != null) {
            try {
                result = Introspection.toInstance(instance, resultType);
            } catch (Exception e) {
                //TODO: Log.w();
            }
        }
        return result;
    }
}
