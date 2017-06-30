package org.hcjf.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 */
public abstract class TtlCollection<K extends Object> {

    private final Map<Long,K> timeWindows;
    private final Long timeWindowsSize;

    public TtlCollection(Long timeWindowsSize) {
        this.timeWindowsSize = timeWindowsSize;
        this.timeWindows = new HashMap<>();
    }

    protected final synchronized void addInstance(K instance) {
        timeWindows.put(System.currentTimeMillis(), instance);
    }

    protected final synchronized void removeOldWindows() {
        Long current = System.currentTimeMillis();
        for(Long time : timeWindows.keySet()) {
            if((time + timeWindowsSize) < current) {
                removeOldInstance(timeWindows.get(time));
            }
        }
    }

    protected abstract void removeOldInstance(K instanceKey);
}
