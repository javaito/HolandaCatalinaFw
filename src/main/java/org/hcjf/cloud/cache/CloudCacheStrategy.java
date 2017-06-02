package org.hcjf.cloud.cache;

import java.util.Set;

/**
 * This interface define the cache strategy implementations.
 * @author javaito
 */
public interface CloudCacheStrategy {

    /**
     * Init the strategy for a particular instance of cache.
     * @param cacheInstance Instance of cache.
     */
    void init(CloudCache cacheInstance);

    /**
     * This method is called when some object was removed to the cache instance.
     * @param id Id removed to the cache instance.
     */
    void onRemove(Object id);

    /**
     * This method is called when some object was added to the cache instance.
     * @param id Id added into the cache instance.
     * @param value Value added into cache.
     */
    void onAdd(Object id, Object value);

    /**
     * This method must evaluate the specific strategy and return all the
     * ids that will be removed of the cache implementation.
     * @return Collection of the ids that will be removed.
     */
    Set<Object> applyStrategy();

}
