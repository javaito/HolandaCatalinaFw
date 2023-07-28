package org.hcjf.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides the mechanism to maintains the wrapped collection
 * elements into the collection some time.
 * @author javaito
 */
public abstract class TtlCollection<K extends Object> {

    private final Map<Long,K> timeWindows;
    private final Long timeWindowsSize;

    public TtlCollection(Long timeWindowsSize) {
        this.timeWindowsSize = timeWindowsSize;
        this.timeWindows = new HashMap<>();
    }

    /**
     * Add instance into the ttl map.
     * @param instance Instance to add.
     */
    protected final synchronized void addInstance(K instance) {
        timeWindows.put(System.currentTimeMillis(), instance);
    }

    /**
     * Removes all the old elements of the wrapped collection.
     */
    protected final synchronized void removeOldWindows() {
        Long current = System.currentTimeMillis();
        for(Iterator<Long> iterator = timeWindows.keySet().iterator(); iterator.hasNext();) {
            Long time = iterator.next();
            if((time + timeWindowsSize) < current) {
                removeOldInstance(timeWindows.get(time));
                iterator.remove();
            }
        }
    }

    /**
     * This implementation remove the specific instance of the wrapped collection.
     * @param instanceKey Instance key.
     */
    protected abstract void removeOldInstance(K instanceKey);
}
