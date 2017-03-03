package org.hcjf.cloud.cache;

import java.util.Set;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public interface CloudCacheStrategy {

    /**
     * This method is called when some object was removed to the cache instance.
     * @param id Id removed to the cache instance.
     */
    public void onRemove(Object id);

    /**
     * This method is called when some object was added to the cache instance.
     * @param id Id added into the cache instance.
     * @param value Value added into cache.
     * @return Return the id of the object to remove, because the strategy need one place to
     * add the new object.
     */
    public Object onAdd(Object id, Object value);

}
