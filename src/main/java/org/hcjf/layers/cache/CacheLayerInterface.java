package org.hcjf.layers.cache;

import org.hcjf.layers.LayerInterface;
import org.hcjf.service.ServiceConsumer;

import java.util.Collection;

/**
 * This interface defines a signature for a cache layer.
 * @param <O> Kind of object that the cache layer use.
 */
public interface CacheLayerInterface<O extends Object> extends LayerInterface, ServiceConsumer {

    /**
     * This method invalidate the values into cache and restore its using the datasource associated to the cache.
     */
    void invalidate();

    /**
     * Returns the timeout value, this value indicates the time that the cache is valid.
     * @return Timeout value.
     */
    Long timout();

    /**
     * This method returns a current values of the cache.
     * @return Current value of the cache.
     */
    O get();

}
