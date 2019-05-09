package org.hcjf.layers.query;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.internal.LinkedTreeMap;
import org.hcjf.bson.BsonArray;
import org.hcjf.bson.BsonDocument;
import org.hcjf.utils.Strings;
import org.hcjf.utils.bson.BsonParcelable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This is a wrapper of map implementation wit the joinable implementation.
 * @author javaito
 *
 */
public class JoinableMap implements Joinable, Groupable, Enlarged, BsonParcelable, Map<String, Object> {

    private static final String RESOURCES_FIELD = "__resources__";
    private static final String MAP_INSTANCE_FIELD = "__map_instance__";

    private final Set<String> resources;
    private final Map<String, Object> mapInstance;
    private Set<String> staticFields;
    private boolean purged = false;
    private Map<String, Object> staticFieldsMap;

    public JoinableMap() {
        this.resources = new TreeSet<>();
        this.mapInstance = new LinkedTreeMap<>();
    }

    public JoinableMap(String resourceName) {
        this.resources = new TreeSet<>();
        this.resources.add(resourceName);
        this.mapInstance = new LinkedHashMap<>();
    }

    public JoinableMap(Map<String, Object> mapInstance, String... fields) {
        this.resources = new TreeSet<>();
        this.mapInstance = new LinkedHashMap<>();
        if(fields != null && fields.length > 0) {
            staticFields = new LinkedHashSet<>();
            for(String field : fields) {
                staticFields.add(field);
            }
            staticFieldsMap = new HashMap<>();
        }
        for(String key : mapInstance.keySet()) {
            if(key.contains(Strings.CLASS_SEPARATOR)) {
                resources.add(key.substring(0, key.lastIndexOf(Strings.CLASS_SEPARATOR)));
            }
            this.put(key, mapInstance.get(key));
        }
    }

    /**
     * Creates a bson document from the JoinableMap instance.
     * @return Bson document instance.
     */
    @Override
    public BsonDocument toBson() {
        purge();
        BsonDocument bsonDocument = BsonParcelable.super.toBson();
        bsonDocument.put(BsonParcelable.super.toBson(MAP_INSTANCE_FIELD, mapInstance));
        bsonDocument.put(BsonParcelable.super.toBson(RESOURCES_FIELD, resources));
        return bsonDocument;
    }

    /**
     * This method populate the joinable map with the information into the bson document.
     * @param document Bson document to populate the parcelable.
     * @param <P> Expected BsonParcelable data type.
     * @return Returns the bson parcelable instance.
     */
    @Override
    public <P extends BsonParcelable> P populate(BsonDocument document) {
        BsonParcelable.super.populate(document);
        BsonDocument bsonDocument = document.get(MAP_INSTANCE_FIELD).getAsDocument();
        this.mapInstance.putAll(fromBson(String.class, Object.class, bsonDocument));
        BsonArray bsonArray = document.get(RESOURCES_FIELD).getAsArray();
        this.resources.addAll(fromBson(Object.class, bsonArray));
        return (P) this;
    }

    /**
     * This method remove all the fields that it's not static
     */
    @Override
    public void purge() {
        if(staticFieldsMap != null) {
            mapInstance.clear();
            mapInstance.putAll(staticFieldsMap);
            staticFieldsMap = null;
            purged = true;
        }
    }

    /**
     * Clone the joinable map instance.
     * @return Joinalbe map clone.
     */
    @Override
    public Enlarged clone(String... fields) {
        return new JoinableMap(this, fields);
    }

    /**
     * Clone the joinable map without domain information.
     * @return Joinable map clone.
     */
    @Override
    public Enlarged cloneEmpty() {
        JoinableMap clone = new JoinableMap(this);
        clone.clear();
        return clone;
    }

    /**
     * Return the value of the field name.
     * @param fieldName Field name.
     * @return Value of the field.
     */
    @Override
    public Object get(String fieldName) {
        Object result = null;
        if(!fieldName.contains(Strings.CLASS_SEPARATOR)) {
            for(String resource : resources) {
                result = mapInstance.get(resource + Strings.CLASS_SEPARATOR + fieldName);
                if(result != null) {
                    break;
                }
            }

            if(result == null) {
                result = mapInstance.get(fieldName);
            }
        } else {
            result = mapInstance.get(fieldName);
        }
        return result;
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

        JoinableMap result = new JoinableMap(new HashMap<>());
        result.resources.addAll(resources);
        result.mapInstance.putAll(mapInstance);
        result.resources.addAll(((JoinableMap)joinable).resources);
        result.mapInstance.putAll(((JoinableMap)joinable));

        return result;
    }

    @Override
    public Groupable group(Groupable groupable) {

        Object instanceValue;
        Object groupableValue;
        GroupableSet groupSet;

        for(String key : groupable.keySet()) {
            if(containsKey(key)) {
                instanceValue = get(key);
                groupableValue = groupable.get(key);
                if(instanceValue instanceof GroupableSet) {
                    ((GroupableSet)instanceValue).add(groupableValue);
                } else if(!get(key).equals(groupable.get(key))) {
                    groupSet = new GroupableSet();
                    groupSet.add(instanceValue);
                    groupSet.add(groupableValue);
                    put(key, groupSet);
                }
            } else {
                put(key, groupable.get(key));
            }
        }

        return this;
    }

    /**
     * Write all the elements of the map.
     * @return Map print.
     */
    @Override
    public final String toString() {
        Strings.Builder builder = new Strings.Builder();
        builder.append(Strings.START_SUB_GROUP);
        for(String key : mapInstance.keySet()) {
            builder.append(key).append(Strings.ASSIGNATION);
            builder.append(mapInstance.get(key), Strings.ARGUMENT_SEPARATOR, Strings.WHITE_SPACE);
        }
        return super.toString();
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
        boolean result = false;
        String fieldName = (String) key;

        if(!fieldName.contains(Strings.CLASS_SEPARATOR)) {
            for(String resource : resources) {
                result = mapInstance.containsKey(resource + Strings.CLASS_SEPARATOR + fieldName);
                if(result) {
                    break;
                }
            }

            if(!result) {
                result = mapInstance.containsKey(fieldName);
            }
        } else {
            result = mapInstance.containsKey(fieldName);
        }
        return result;
    }

    @Override
    public boolean containsValue(Object value) {
        return mapInstance.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return get(key.toString());
    }

    @Override
    public Object put(String key, Object value) {
        if(key.contains(Strings.CLASS_SEPARATOR)) {
            resources.add(key.substring(0, key.lastIndexOf(Strings.CLASS_SEPARATOR)));
        }
        if(staticFields != null && staticFields.contains(key)) {
            staticFieldsMap.put(key, value);
        }
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
        return purged ? staticFields : mapInstance.keySet();
    }

    @Override
    public Collection<Object> values() {
        return mapInstance.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> result;
        if(purged) {
            result = new LinkedHashSet();
            for (String key : keySet()) {
                result.add(new JoinableEntry<>(key, get(key)));
            }
        } else {
            result = mapInstance.entrySet();
        }
        return result;
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

    /**
     * This private class is only to knows if the set implementation
     * into the groupable object is because the groupable object was grouped
     * whit other instance or the set is domains information.
     */
    public static final class GroupableSet extends HashSet<Object> {}

    public static final class JoinableEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private V value;

        public JoinableEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }
}
