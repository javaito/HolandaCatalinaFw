package org.hcjf.layers.cache;

import org.hcjf.layers.Layer;

import java.util.Map;

public class SubLayerCache extends Layer implements CacheLayerInterface<Map<String,Object>> {

    private Map<String,Object> value;
    private CacheServiceConsumer cacheServiceConsumer;


    @Override
    public String getImplName() {
        return "subLayer";
    }

    /**
     * This method invalidate the values into cache and restore its using the datasource associated to the cache.
     */
    @Override
    public void invalidate() {
        if(cacheServiceConsumer == null) {
            cacheServiceConsumer = new CacheServiceConsumer("map");
            CacheService.getInstance().registerConsumer(cacheServiceConsumer);
        }
        value = cacheServiceConsumer.get();
    }

    /**
     * Returns the timeout value, this value indicates the time that the cache is valid.
     *
     * @return Timeout value.
     */
    @Override
    public Long timout() {
        return 5000L;
    }

    /**
     * This method returns a current values of the cache.
     *
     * @return Current value of the cache.
     */
    @Override
    public Map<String, Object> get() {
        return value;
    }
}
