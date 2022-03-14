package org.hcjf.layers.cache;

import org.hcjf.layers.Layers;
import org.hcjf.service.ServiceConsumer;

public class CacheServiceConsumer implements ServiceConsumer {

    private final String cacheImpl;

    public CacheServiceConsumer(String cacheImpl) {
        this.cacheImpl = cacheImpl;
    }

    public String getCacheImpl() {
        return cacheImpl;
    }

    public <O extends Object> O get() {
        return (O) Layers.get(CacheLayerInterface.class, getCacheImpl()).get();
    }
}
