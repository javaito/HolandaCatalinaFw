package org.hcjf.cloud;

/**
 * This interface provides the method to use a simple cache.
 * @author javaito
 * @email javaito@gmail.com
 */
public interface CloudCache {

    /**
     * Remove the information associated to the specific id from the cache.
     * @param id Information index.
     */
    public void remove(Object id);

    /**
     * Add some information indexed by specific id onto the cloud cache implementation.
     * @param id Information index.
     * @param value Information to put into cache.
     */
    public void add(Object id, Object value);

    /**
     * Return the information indexed by the specific id.
     * @param id Information index.
     * @param <O> Expected kind of information.
     * @return Return the information associated to the specific id.
     */
    public <O extends Object> O get(Object id);
}
