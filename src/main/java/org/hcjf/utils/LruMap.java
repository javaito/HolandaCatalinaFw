package org.hcjf.utils;

import org.hcjf.properties.SystemProperties;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This map implementation use an algorithm to maintains a fixed size into the map with the elements
 * least recently used (LRU).
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class LruMap<K extends Object, V extends Object> implements Map<K,V> {

    private Integer maxSize;
    private final List<Key<K>> keys;
    private final Map<K,Key<K>> metadata;
    private final Map<K,V> mapInstance;
    private final List<RemoveOverflowListener<K, V>> listeners;

    public LruMap() {
        this(SystemProperties.getInteger(SystemProperties.HCJF_DEFAULT_LRU_MAP_SIZE));
    }

    public LruMap(Integer maxSize) {
        this.keys = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.mapInstance = new HashMap<>();
        this.maxSize = maxSize;
        this.listeners = new ArrayList<>();
    }

    /**
     * Add a listener remove overflow listener.
     * @param listener Listener instance.
     */
    public final void addRemoveOverflowListener(RemoveOverflowListener<K,V> listener) {
        if(listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Returns the max size of the map.
     * @return Max size of the map.
     */
    public final Integer getMaxSize() {
        return maxSize;
    }

    /**
     * Set the max size of the map.
     * @param maxSize Max size of the map.
     */
    public synchronized final void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
        removeOverflow();
    }

    /**
     * Update the temporal component into the keys.
     * @param keys Keys to updateMetadata.
     */
    private void updateMetadata(Key<K>... keys) {
        for(Key<K> key : keys) {
            key.update();
        }
        Collections.sort(this.keys);
    }

    /**
     * This method remove the overflow elements into the map.
     */
    private void removeOverflow() {
        for (int i = 0; i < keys.size() - maxSize; i++) {
            Key<K> key = keys.remove(keys.size() -1);
            metadata.remove(key.getKey());
            V value = mapInstance.remove(key.getKey());
            listeners.forEach(L -> L.onRemove(key.getKey(), value));
        }
    }

    @Override
    public int size() {
        return mapInstance.size();
    }

    @Override
    public boolean isEmpty() {
        return mapInstance.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return mapInstance.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mapInstance.containsValue(value);
    }

    @Override
    public synchronized V get(Object key) {
        V result = null;
        if(mapInstance.containsKey(key)) {
            updateMetadata(metadata.get(key));
            result = mapInstance.get(key);
        }
        return result;
    }

    @Override
    public synchronized V put(K key, V value) {
        V result = mapInstance.put(key, value);
        Key<K> temporalKey = new Key<>(key);
        keys.add(temporalKey);
        metadata.put(key, temporalKey);
        updateMetadata();
        removeOverflow();
        return result;
    }

    @Override
    public synchronized V remove(Object key) {
        V result = mapInstance.remove(key);
        keys.remove(metadata.remove(key));
        updateMetadata();
        return result;
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> m) {
        mapInstance.putAll(m);
        Key<K> temporalKey;
        for(K key : m.keySet()) {
            temporalKey = new Key<>(key);
            keys.add(temporalKey);
            metadata.put(key, temporalKey);
        }
        updateMetadata();
        removeOverflow();
    }

    @Override
    public synchronized void clear() {
        mapInstance.clear();
        metadata.clear();
        keys.clear();
    }

    @Override
    public Set<K> keySet() {
        return mapInstance.keySet();
    }

    @Override
    public Collection<V> values() {
        return mapInstance.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return mapInstance.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return mapInstance.equals(o);
    }

    @Override
    public int hashCode() {
        return mapInstance.hashCode();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return mapInstance.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        mapInstance.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        mapInstance.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return mapInstance.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return mapInstance.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return mapInstance.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return mapInstance.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return mapInstance.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return mapInstance.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return mapInstance.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return mapInstance.merge(key, value, remappingFunction);
    }

    /**
     * This class represents a key with a temporal component in order to knows what key is older than other.
     * @param <K> Key type.
     */
    private static final class Key<K extends Object> implements Comparable<Key> {

        private final K key;
        private Long lastUpdate;

        public Key(K key) {
            this.key = key;
            this.lastUpdate = System.currentTimeMillis();
        }

        public K getKey() {
            return key;
        }

        public void update() {
            lastUpdate = System.currentTimeMillis();
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = false;
            if(obj instanceof Key) {
                result = key.equals(((Key)obj).key);
            }
            return result;
        }

        @Override
        public String toString() {
            return key.toString();
        }

        @Override
        public int compareTo(Key o) {
            return lastUpdate.compareTo(o.lastUpdate) * -1;
        }
    }

    /**
     * This interface provides the method to listener when an object is deleted because is part fo the overflow.
     * @param <K> Expected key data type.
     * @param <V> Expected value data type.
     */
    public interface RemoveOverflowListener<K extends Object, V extends Object> {

        void onRemove(K key, V value);

    }
}
