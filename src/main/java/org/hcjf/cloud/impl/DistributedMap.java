package org.hcjf.cloud.impl;

import org.hcjf.cloud.impl.network.CloudOrchestrator;
import org.hcjf.cloud.impl.objects.DistributedTree;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author javaito
 */
public class DistributedMap<K extends Object, V extends Object> implements Map<K, V> {

    private String name;

    public DistributedMap(String name) {
        this.name = name;
        CloudOrchestrator.getInstance().publishPath(Map.class.getName(), name);
    }

    @Override
    public int size() {
        DistributedTree tree = CloudOrchestrator.getInstance().invoke(Map.class.getName(), name);
        return tree.size();
    }

    @Override
    public boolean isEmpty() {
        DistributedTree tree = CloudOrchestrator.getInstance().invoke(Map.class.getName(), name);
        return tree.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        DistributedTree tree = CloudOrchestrator.getInstance().invoke(Map.class.getName(), name);
        return tree.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return CloudOrchestrator.getInstance().invoke(Map.class.getName(), name, key);
    }

    @Override
    public V put(K key, V value) {
        CloudOrchestrator.getInstance().publishObject(value, System.currentTimeMillis(), Map.class.getName(), name, key);
        return value;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}
