package org.hcjf.layers.cache;

import org.hcjf.layers.Layer;

import java.util.HashMap;
import java.util.Map;

public class MapCache extends Layer implements CacheLayerInterface<Map<String,Object>> {

    private Map<String,Object> value;

    @Override
    public String getImplName() {
        return "map";
    }

    /**
     * This method invalidate the values into cache and restore its using the datasource associated to the cache.
     */
    @Override
    public void invalidate() {
        value = new HashMap<>(Source.getInstance().getSource());
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

    public static final class Source {

        private static final Source instance = new Source();
        private Map<String,Object> source;

        private Source() {
            this.source = new HashMap<>();
        }

        public static final Source getInstance() {
            return instance;
        }

        public Map<String, Object> getSource() {
            return source;
        }
    }
}
