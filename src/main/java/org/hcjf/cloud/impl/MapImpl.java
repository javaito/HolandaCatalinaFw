package org.hcjf.cloud.impl;

import org.hcjf.cloud.impl.network.CloudOrchestrator;
import org.hcjf.cloud.impl.objects.DistributedTree;

import java.util.*;

/**
 * @author javaito
 */
public class MapImpl<K extends Object, V extends Object> implements Map<K, V> {

    private String name;

    public MapImpl(String name) {
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
        boolean result = false;
        for(Object mapValue : values()) {
            if(result = mapValue.equals(value)) {
                break;
            }
        }
        return result;
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
        V result = CloudOrchestrator.getInstance().invoke(Map.class.getName(), name, key);
        CloudOrchestrator.getInstance().hidePath(Map.class.getName(), name, key);
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        for(K key : keySet()) {
            CloudOrchestrator.getInstance().hidePath(Map.class.getName(), name, key);
        }
    }

    @Override
    public Set<K> keySet() {
        DistributedTree tree = CloudOrchestrator.getInstance().invoke(Map.class.getName(), name);
        return tree.keySet();
    }

    @Override
    public Collection<V> values() {
        Collection<V> result = new ArrayList<>();
        for(K key : keySet()) {
            result.add(get(key));
        }
        return result;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K,V>> result = new HashSet<>();
        for(K key : keySet()) {
            result.add(new Entry<>() {
                @Override
                public K getKey() {
                    return key;
                }

                @Override
                public V getValue() {
                    return CloudOrchestrator.getInstance().invoke(Map.class.getName(), name, key);
                }

                @Override
                public V setValue(V value) {
                    CloudOrchestrator.getInstance().publishObject(value,
                            System.currentTimeMillis(), Map.class.getName(), name, key);
                    return value;
                }
            });
        }
        return result;
    }
}
