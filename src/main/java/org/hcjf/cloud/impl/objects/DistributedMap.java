package org.hcjf.cloud.impl.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author javaito
 */
public class DistributedMap<K extends Object, V extends Object> implements Map<K, V> {

    private final Set<K> keys;
    private final Map<K,V> localStore;

    public DistributedMap(Map<K, V> localStore) {
        this.localStore = localStore;
        this.keys = new HashSet<>();
        this.keys.addAll(localStore.keySet());
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return localStore.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return localStore.get(key);
    }

    @Override
    public V put(K key, V value) {
        return localStore.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return localStore.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        localStore.putAll(m);
    }

    @Override
    public void clear() {
        localStore.clear();
    }

    @Override
    public Set<K> keySet() {
        return localStore.keySet();
    }

    @Override
    public Collection<V> values() {
        return localStore.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return localStore.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return localStore.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        localStore.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        localStore.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return localStore.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return localStore.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return localStore.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return localStore.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return localStore.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return localStore.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return localStore.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return localStore.merge(key, value, remappingFunction);
    }
}
