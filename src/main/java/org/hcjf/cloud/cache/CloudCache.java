package org.hcjf.cloud.cache;

import org.hcjf.cloud.Cloud;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.util.*;
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

        strategies.forEach(S -> S.init(this));
    }

    /**
     * Return the name of the cache.
     * @return Name of the cache.
     */
    public final String getCacheName() {
        return cacheName;
    }

    /**
     * Apply all the strategies of the cache instance an remove all
     * the instances that the strategies return.
     */
    private void applyStrategies() {
        Set<Object> ids = new TreeSet<>();
        strategies.forEach(S -> ids.addAll(S.applyStrategy()));
        ids.forEach(instances::remove);
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
                strategies.forEach(S -> S.onRemove(id));
                applyStrategies();
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
            strategies.forEach(S -> S.onAdd(id, value));
            applyStrategies();
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
        applyStrategies();
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
