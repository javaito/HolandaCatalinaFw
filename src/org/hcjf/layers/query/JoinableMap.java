package org.hcjf.layers.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This is a wrapper of map implementation wit the joinable implementation.
 * @author javaito
 * @email javaito@gmail.com
 */
public class JoinableMap implements Joinable, Map<String, Object> {

    private final Map<String, Object> mapInstance;

    public JoinableMap() {
        mapInstance = new HashMap<>();
    }

    public JoinableMap(Map<String, Object> mapInstance) {
        this.mapInstance = mapInstance;
    }

    /**
     * Return the value of the field name.
     * @param fieldName Field name.
     * @return Value of the field.
     */
    @Override
    public Object get(String fieldName) {
        return mapInstance.get(fieldName);
    }

    /**
     * Join the information stored into this instance of the joinable with the
     * informacion stored into the joinable parameter.
     * @param joinable Joinable parameter.
     * @return Return this instance of the joinable.
     * @throws IllegalArgumentException if the joinable parameter is not a JoinableMap instance.
     * @throws NullPointerException if the joinable parameter is null.
     */
    @Override
    public Joinable join(Joinable joinable) {
        if(joinable == null) {
            throw new NullPointerException("Try to join with null joinable.");
        }

        if(!(joinable instanceof JoinableMap)) {
            throw new IllegalArgumentException("Only support JoinableMap instance.");
        }

        mapInstance.putAll(((JoinableMap)joinable));
        return this;
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
    public Object get(Object key) {
        return mapInstance.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return mapInstance.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return mapInstance.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        mapInstance.putAll(m);
    }

    @Override
    public void clear() {
        mapInstance.clear();
    }

    @Override
    public Set<String> keySet() {
        return mapInstance.keySet();
    }

    @Override
    public Collection<Object> values() {
        return mapInstance.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
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
    public Object getOrDefault(Object key, Object defaultValue) {
        return mapInstance.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        mapInstance.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        mapInstance.replaceAll(function);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        return mapInstance.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return mapInstance.remove(key, value);
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        return mapInstance.replace(key, oldValue, newValue);
    }

    @Override
    public Object replace(String key, Object value) {
        return mapInstance.replace(key, value);
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        return mapInstance.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return mapInstance.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return mapInstance.compute(key, remappingFunction);
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return mapInstance.merge(key, value, remappingFunction);
    }
}
