package org.hcjf.layers.query;

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
    private static final String RESOURCE_FIELD_PATTERN = "%s.%s";

    private final Set<String> resources;
    private final Map<String, Object> mapInstance;
    private final Map<String, Map<String,Object>> mapInstanceByResource;
    private Set<String> staticFields;
    private boolean purged = false;
    private Map<String, Object> staticFieldsMap;

    public JoinableMap() {
        this.resources = new TreeSet<>();
        this.mapInstance = new LinkedTreeMap<>();
        this.mapInstanceByResource = new LinkedHashMap<>();
        this.staticFields = new LinkedHashSet<>();
        this.staticFieldsMap = new HashMap<>();
    }

    public JoinableMap(String resourceName) {
        this.resources = new TreeSet<>();
        this.resources.add(resourceName);
        this.mapInstance = new LinkedHashMap<>();
        this.mapInstanceByResource = new LinkedHashMap<>();
        this.mapInstanceByResource.put(resourceName, new HashMap<>());
    }

    public JoinableMap(Map<String, Object> mapInstance, String... fields) {
        this.resources = new TreeSet<>();
        this.mapInstance = new LinkedHashMap<>();
        this.mapInstanceByResource = new LinkedHashMap<>();
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
        JoinableMap result = new JoinableMap(new HashMap<>(), fields);
        result.resources.addAll(resources);
        result.mapInstance.putAll(mapInstance);
        result.mapInstanceByResource.putAll(mapInstanceByResource);
        return result;
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
        Object result;
        String resourceName = getResourceNameForPath(fieldName);
        if(resourceName != null) {
            String fieldWithoutResource = getFieldWithoutResource(resourceName, fieldName);
            result = mapInstanceByResource.get(resourceName).get(fieldWithoutResource);
        } else {
            result = mapInstance.get(fieldName);
        }
        return result;
    }

    public void setResource(String resourceName) {
        if(!containsResource(resourceName)) {
            resources.add(resourceName);
            mapInstanceByResource.put(resourceName, new HashMap<>(mapInstance));
        }
    }

    /**
     * Join the information stored into this instance of the joinable with the
     * informacion stored into the joinable parameter.
     * @param leftResource Name of the left resource of the join.
     * @param rightResource Name of the right resource of the join.
     * @param joinable Joinable parameter.
     * @return Return this instance of the joinable.
     * @throws IllegalArgumentException if the joinable parameter is not a JoinableMap instance.
     * @throws NullPointerException if the joinable parameter is null.
     */
    @Override
    public Joinable join(String leftResource, String rightResource, Joinable joinable) {
        if(joinable == null) {
            throw new NullPointerException("Try to join with null joinable.");
        }

        if(!(joinable instanceof JoinableMap)) {
            throw new IllegalArgumentException("Only support JoinableMap instance.");
        }

        JoinableMap result;
        if(mapInstanceByResource.containsKey(leftResource)) {
            result = new JoinableMap();
            result.resources.addAll(resources);
            result.mapInstance.putAll(mapInstance);
            if(mapInstanceByResource.size() > 1) {
                result.mapInstanceByResource.putAll(mapInstanceByResource);
            } else {
                Map<String,Object> mapInstanceCopy = new HashMap<>(mapInstance);
                result.mapInstanceByResource.put(leftResource, mapInstanceCopy);
            }
        } else {
            result = new JoinableMap(leftResource);
            result.mapInstance.putAll(mapInstance);
            result.mapInstanceByResource.get(leftResource).putAll(mapInstance);
        }

        result.resources.add(rightResource);
        result.mapInstanceByResource.put(rightResource, new HashMap<>());
        result.mapInstanceByResource.get(rightResource).putAll(((JoinableMap)joinable));

        for(Entry<String,Object> entry : ((JoinableMap)joinable).entrySet()) {
            if(result.mapInstance.containsKey(entry.getKey())) {
                result.mapInstance.put(String.format(RESOURCE_FIELD_PATTERN, rightResource, entry.getKey()), entry.getValue());
            } else {
                result.mapInstance.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Verify if the joinable map instance contains a specific resource.
     * @param resourceName Resource name
     * @return Return true of the resource is present into the instance nad false in the otherwise.
     */
    public boolean containsResource(String resourceName) {
        return mapInstanceByResource.containsKey(resourceName);
    }

    /**
     * Returns a part of the model that represents a specific resource.
     * @param resourceName Resource name.
     * @return Part of the model.
     */
    public Map<String,Object> getResourceModel(String resourceName) {
        return Collections.unmodifiableMap(mapInstanceByResource.get(resourceName) != null ?
                mapInstanceByResource.get(resourceName) : new HashMap<>());
    }

    public final String getResourceNameForPath(String path) {
        String result = null;

        if(path.contains(Strings.CLASS_SEPARATOR)) {
            for(String resource : resources) {
                if(path.startsWith(resource)) {
                    String possibleResource = resource;
                    if(result == null) {
                        result = possibleResource;
                    } else {
                        if(result.length() < possibleResource.length()) {
                            result = possibleResource;
                        }
                    }
                }
            }
        }

        return result;
    }

    public final String getFieldWithoutResource(String resourceName, String path) {
        return resourceName == null || resourceName.isBlank() ? path :
                path.replace(resourceName + Strings.CLASS_SEPARATOR, Strings.EMPTY_STRING);
    }

    /**
     * Returns the set of resources.
     * @return Set of resources.
     */
    public Set<String> getResources() {
        return Collections.unmodifiableSet(resources);
    }

    @Override
    public Groupable group(Groupable groupable) {

        Object instanceValue;
        Object groupableValue;
        GroupableSet groupSet;

        for(String key : groupable.keySet()) {

            String resource = getResourceNameForPath(key);
            String keyWithoutResource = getFieldWithoutResource(resource, key);
            if(resource == null) {
                for(String candidateResource : getResources()) {
                    if(getResourceModel(candidateResource).containsKey(key)) {
                        resource = candidateResource;
                        break;
                    }
                }
            }
            if(resource == null && resources != null && !resources.isEmpty()) {
                resource = getResources().stream().findFirst().get();
            }

            if(containsKey(key)) {
                instanceValue = get(key);
                groupableValue = groupable.get(key);
                if(instanceValue instanceof GroupableSet) {
                    ((GroupableSet)instanceValue).add(groupableValue);
                    if(resource != null) {
                        ((GroupableSet) mapInstanceByResource.get(resource).get(keyWithoutResource)).add(groupableValue);
                    }
                } else {
                    groupSet = new GroupableSet();
                    groupSet.add(instanceValue);
                    groupSet.add(groupableValue);
                    put(key, groupSet);
                    if(resource != null) {
                        groupSet = new GroupableSet();
                        groupSet.add(instanceValue);
                        groupSet.add(groupableValue);
                        mapInstanceByResource.get(resource).put(keyWithoutResource, groupSet);
                    }
                }
            } else {
                put(key, groupable.get(key));
                if(resource != null) {
                    mapInstanceByResource.get(resource).put(keyWithoutResource, groupable.get(key));
                }
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
        String resourceName = getResourceNameForPath(fieldName);
        if(resourceName != null) {
            String fieldNameWithoutResource = getFieldWithoutResource(resourceName, fieldName);
            result = mapInstanceByResource.get(resourceName).containsKey(fieldNameWithoutResource);
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
        String resourceName = getResourceNameForPath(key);
        if(resourceName != null) {
            String keyWithoutResource = getFieldWithoutResource(resourceName, key);
            mapInstanceByResource.get(resourceName).put(keyWithoutResource, value);
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
    public static final class GroupableSet extends ArrayList<Object> {}

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
