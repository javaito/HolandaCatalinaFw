package org.hcjf.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class implements the map interface to wrap some other instance of map,
 * and maintains the entries into the map some time and then the elements are removed.
 * @author javaito
 */
public final class TtlMap<K extends Object, V extends Object> extends TtlCollection<K> implements Map<K,V> {

    public final Map<K,V> instance;

    public TtlMap(Map<K,V> instance, Long timeWindowsSize) {
        super(timeWindowsSize);
        this.instance = instance;
    }

    @Override
    protected void removeOldInstance(K instanceKey) {
        instance.remove(instanceKey);
    }

    @Override
    public int size() {
        removeOldWindows();
        return instance.size();
    }

    @Override
    public boolean isEmpty() {
        removeOldWindows();
        return instance.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        removeOldWindows();
        return instance.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        removeOldWindows();
        return instance.containsValue(value);
    }

    @Override
    public V get(Object key) {
        removeOldWindows();
        return instance.get(key);
    }

    @Override
    public V put(K key, V value) {
        removeOldWindows();
        addInstance(key);
        return instance.put(key, value);
    }

    @Override
    public V remove(Object key) {
        removeOldWindows();
        return instance.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        instance.clear();
    }

    @Override
    public Set<K> keySet() {
        removeOldWindows();
        return instance.keySet();
    }

    @Override
    public Collection<V> values() {
        removeOldWindows();
        return instance.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        removeOldWindows();
        return instance.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return instance.equals(o);
    }

    @Override
    public int hashCode() {
        return instance.hashCode();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return instance.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        removeOldWindows();
        instance.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        removeOldWindows();
        instance.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        removeOldWindows();
        V result = instance.putIfAbsent(key, value);
        if(result != null) {
            addInstance(key);
        }
        return result;
    }

    @Override
    public boolean remove(Object key, Object value) {
        removeOldWindows();
        return instance.remove(key, value);

    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        removeOldWindows();
        return instance.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        removeOldWindows();
        return instance.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        removeOldWindows();
        V result = instance.computeIfAbsent(key, mappingFunction);
        if(result != null) {
            addInstance(key);
        }
        return result;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        removeOldWindows();
        V result = instance.computeIfPresent(key, remappingFunction);
        if(result != null) {
            addInstance(key);
        }
        return result;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        removeOldWindows();
        addInstance(key);
        return instance.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        removeOldWindows();
        addInstance(key);
        return instance.merge(key, value, remappingFunction);
    }
}
