package org.hcjf.cloud.cache;

import org.hcjf.cloud.Cloud;
import org.hcjf.properties.SystemProperties;

import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This strategy maintains the size of the internal storage.
 * @author javaito
 */
public class SizeStrategy implements CloudCacheStrategy {

    private final Integer size;
    private Queue<Object> ids;

    public SizeStrategy(Integer size) {
        this.size = size;
    }

    /**
     * This method initialize the distributed map to maintains the size of the cache.
     * @param cacheInstance Instance of cache.
     */
    @Override
    public void init(CloudCache cacheInstance) {
        ids = Cloud.getQueue(SystemProperties.get(SystemProperties.Cloud.Cache.SIZE_STRATEGY_MAP_SUFFIX_NAME) +
                        cacheInstance.getCacheName());
    }

    /**
     * Remove a specific id of the internal collection.
     * @param id Id removed to the cache instance.
     */
    @Override
    public void onRemove(Object id) {
        ids.remove(id);
    }

    /**
     * Add a new id into the internal collection.
     * @param id Id added into the cache instance.
     * @param value Value added into cache.
     * @return Return only the last id of the internal collection.
     */
    @Override
    public void onAdd(Object id, Object value) {
        //If the id is contained into the ids collection then this id is removed
        //because the id is added in the next step.
        if(ids.contains(id)) {
            ids.remove(id);
        }

        //Add the id into the collection and if the collection size if bigger than
        //the max size specified then the last value of the collection is removed
        ids.offer(value);
    }

    /**
     * This method must evaluate the specific strategy and return all the
     * ids that will be removed of the cache implementation.
     * @return Collection of the ids that will be removed.
     */
    @Override
    public Set<Object> applyStrategy() {
        Set<Object> result = null;

        if(ids.size() > size) {
            result = ids.stream().skip(size).collect(Collectors.toSet());
        }

        return result;
    }
}
