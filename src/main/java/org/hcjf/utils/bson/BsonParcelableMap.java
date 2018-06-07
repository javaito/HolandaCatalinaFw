package org.hcjf.utils.bson;

import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonElement;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author javaito
 */
public class BsonParcelableMap implements BsonParcelable, Map<String,Object> {

    private final Map<String,Object> internalInstance;

    public BsonParcelableMap() {
        this.internalInstance = new HashMap<>();
    }

    /**
     * Populate the current instance of parcelable with all the information into the bson document.
     * @param document Bson document to populate the parcelable.
     * @param <P> Expected parcelable type.
     * @return Return the same instance that was populated.
     */
    @Override
    public <P extends BsonParcelable> P populate(BsonDocument document) {
        String fieldName;
        BsonElement element;
        Iterator<String> fieldsIterator = document.iterator();
        while(fieldsIterator.hasNext()) {
            try {
                fieldName = fieldsIterator.next();
                element = document.get(fieldName);
                put(fieldName, fromBson(Object.class, Object.class, Object.class, element));
            } catch (Exception ex){}
        }
        return (P) this;
    }

    @Override
    public int size() {
        return internalInstance.size();
    }

    @Override
    public boolean isEmpty() {
        return internalInstance.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return internalInstance.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internalInstance.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return internalInstance.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return internalInstance.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return internalInstance.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        internalInstance.putAll(m);
    }

    @Override
    public void clear() {
        internalInstance.clear();
    }

    @Override
    public Set<String> keySet() {
        return internalInstance.keySet();
    }

    @Override
    public Collection<Object> values() {
        return internalInstance.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return internalInstance.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return internalInstance.equals(o);
    }

    @Override
    public int hashCode() {
        return internalInstance.hashCode();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return internalInstance.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        internalInstance.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        internalInstance.replaceAll(function);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        return internalInstance.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return internalInstance.remove(key, value);
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        return internalInstance.replace(key, oldValue, newValue);
    }

    @Override
    public Object replace(String key, Object value) {
        return internalInstance.replace(key, value);
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        return internalInstance.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return internalInstance.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return internalInstance.compute(key, remappingFunction);
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return internalInstance.merge(key, value, remappingFunction);
    }

}
