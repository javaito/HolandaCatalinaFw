package org.hcjf.utils.bson;

import org.hcjf.bson.*;
import org.hcjf.layers.Layers;
import org.hcjf.utils.Introspection;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * This interface provides the default method to create a bson document of the instance and
 * recreate the instance from bson document.
 * @author javaito.
 */
public interface BsonParcelable {

    String MAP_KEYS_FIELD_NAME = "__K__";
    String MAP_VALUES_FIELD_NAME = "__V__";
    String PARCELABLE_CLASS_NAME = "__pcn__";

    /**
     * Returns the bson representation of the instance.
     * @return Bson representation.
     */
    default BsonDocument toBson() {
        BsonDocument document = new BsonDocument();
        document.put(PARCELABLE_CLASS_NAME, getClass().getName());
        Map<String, Introspection.Accessors> accessorsMap = Introspection.getAccessors(getClass());
        Introspection.Getter getter;
        Object value;
        for(Introspection.Accessors accessors : accessorsMap.values()) {
            try {
                getter = accessors.getGetter();
                if(getter != null) {
                    value = getter.get(this);
                    if(value != null) {
                        document.put(toBson(accessors.getResourceName(), value));
                    }
                }
            } catch (Exception ex){}
        }
        return document;
    }

    /**
     * Returns a bson representation of a map.
     * @param name Name of the map.
     * @param map Map instance.
     * @return Bson representation.
     */
    default BsonDocument toBson(String name, Map map) {
        BsonDocument document = new BsonDocument(name);
        List keys = new ArrayList();
        List values = new ArrayList();

        for(Object key : map.keySet()) {
            keys.add(key);
            values.add(map.get(key));
        }

        document.put(toBson(MAP_KEYS_FIELD_NAME, keys));
        document.put(toBson(MAP_VALUES_FIELD_NAME, values));

        return document;
    }

    /**
     * Returns a bson representation of a collection.
     * @param name Name of the collection.
     * @param collection Collection instance.
     * @return Bson representation.
     */
    default BsonArray toBson(String name, Collection collection) {
        BsonArray array = new BsonArray(name);
        for(Object value : collection) {
            array.add(toBson(null, value));
        }
        return array;
    }

    /**
     * Returns a bson representation of a object.
     * @param name Name of the object.
     * @param value Object instance.
     * @return Bson representation.
     */
    default BsonElement toBson(String name, Object value) {
        BsonElement result;
        if(value == null) {
            result = new BsonPrimitive(name, null);
        } else if(BsonParcelable.class.isAssignableFrom(value.getClass())) {
            BsonDocument document = ((BsonParcelable) value).toBson();
            document.setName(name);
            result = document;
        } else if(Collection.class.isAssignableFrom(value.getClass())) {
            result = toBson(name, (Collection)value);
        } else if(Map.class.isAssignableFrom(value.getClass())) {
            result = toBson(name, (Map)value);
        } else if(byte[].class.equals(value.getClass())) {
            result = new BsonPrimitive(name, value);
        } else if(value.getClass().isArray()) {
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < Array.getLength(value); i++) {
                arrayList.add(Array.get(value, i));
            }
            result = toBson(name, arrayList);
        } else if(value.getClass().isEnum()) {
            result = new BsonPrimitive(name, value.toString());
        } else if(Class.class.equals(value.getClass())) {
            result = new BsonPrimitive(name, ((Class)value).getName());
        } else if(BsonType.fromValue(value) != null) {
            result = new BsonPrimitive(name, value);
        } else if(Serializable.class.isAssignableFrom(value.getClass())) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(value);
                oos.flush();
                result = new BsonPrimitive(name, baos.toByteArray());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    /**
     * Populate the current instance of parcelable with all the information into the bson document.
     * @param document Bson document to populate the parcelable.
     * @param <P> Expected parcelable type.
     * @return Return the same instance that was populated.
     */
    default <P extends BsonParcelable> P populate(BsonDocument document) {
        Map<String, Introspection.Accessors> accessorsMap = Introspection.getAccessors(getClass());

        Introspection.Setter setter;
        BsonElement element;
        for(Introspection.Accessors accessors : accessorsMap.values()) {
            try {
                setter = accessors.getSetter();
                if(setter != null) {
                    element = document.get(accessors.getResourceName());
                    if(element != null) {
                        setter.set(this, fromBson(setter.getParameterType(),
                                setter.getParameterKeyType(),
                                setter.getParameterCollectionType(), element));
                    }
                }
            } catch (Exception ex){}
        }

        return (P) this;
    }

    /**
     * Returns a map instance from a bson document.
     * @param document Bson document to create the map instance.
     * @param expectedKeyType Expected key type.
     * @param expectedValueType Expected value type.
     * @return Map instance.
     */
    default Map fromBson(Class expectedKeyType, Class expectedValueType, BsonDocument document) {
        Map result = new HashMap<>();
        if(document.hasElement(MAP_KEYS_FIELD_NAME)) {
            BsonArray keys = document.get(MAP_KEYS_FIELD_NAME).getAsArray();
            BsonArray values = document.get(MAP_VALUES_FIELD_NAME).getAsArray();

            Object key;
            Object value;

            for (int i = 0; i < keys.size(); i++) {
                key = fromBson(expectedKeyType,
                        null, null, keys.get(i));
                value = fromBson(expectedValueType,
                        null, null, values.get(i));
                result.put(key, value);
            }
        } else {
            for(String key : document) {
                result.put(key, fromBson(null,
                        null, null, document.get(key)));
            }
        }

        return result;
    }

    /**
     * Returns a collection instance from a bson array.
     * @param array Bson array to create the collection instance.
     * @param expectedCollectionType Expected data type for map's or collection's values.
     * @return Collection instance.
     */
    default Collection fromBson(Class expectedCollectionType, BsonArray array) {
        List result = new ArrayList();
        for (int i = 0; i < array.size(); i++) {
            result.add(fromBson(expectedCollectionType,
                    null, null, array.get(i)));
        }
        return result;
    }

    /**
     * Creates a instance using the expected data type and bson element.
     * @param expectedDataType Expected result data type.
     * @param element Bson element.
     * @param keyType Expected data type for map's key.
     * @param collectionDataType Expected data type for map's or collection's values.
     * @return Object instance.
     */
    default Object fromBson(Class expectedDataType, Class keyType, Class collectionDataType, BsonElement element) {
        Object result;

        //Verify data type.
        expectedDataType = typeFromBson(expectedDataType, element);

        if(expectedDataType == null) {
            result = null;
        } else if (BsonParcelable.class.isAssignableFrom(expectedDataType) && element instanceof BsonDocument) {
            result = Builder.create((BsonDocument)element);
        } else if (Collection.class.isAssignableFrom(expectedDataType) && element instanceof BsonArray) {
            result = fromBson(collectionDataType, (BsonArray) element);
        } else if (expectedDataType.isArray() && element instanceof BsonArray) {
            Collection collection = fromBson(expectedDataType.getComponentType(), (BsonArray) element);
            result = collection.toArray((Object[]) Array.newInstance(
                    expectedDataType.getComponentType(), collection.size()));
        } else if (Map.class.isAssignableFrom(expectedDataType) && element instanceof BsonDocument) {
            result = fromBson(keyType, collectionDataType, (BsonDocument) element);
        } else if (expectedDataType.isEnum() && element instanceof BsonPrimitive) {
            result = Enum.valueOf(expectedDataType, element.getAsString());
        } else if (expectedDataType.equals(Class.class) && element instanceof BsonPrimitive) {
            try {
                result = Class.forName(element.getAsString());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException();
            }
        } else if (byte[].class.equals(expectedDataType)) {
            if(byte[].class.equals(element.getValue().getClass())) {
                result = element.getValue();
            } else if(ByteBuffer.class.isAssignableFrom(element.getValue().getClass())) {
                result = ((ByteBuffer)element.getValue()).array();
            } else {
                result = element.getValue();
            }
        } else if (Serializable.class.isAssignableFrom(expectedDataType) && !UUID.class.isAssignableFrom(expectedDataType) &&
                element instanceof BsonPrimitive && ((BsonPrimitive) element).getType().equals(BsonType.BINARY)) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(element.getAsBytes());
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                result = ois.readObject();
            } catch (Exception ex) {
                result = element.getValue();
            }
        } else {
            result = element.getValue();
        }

        return result;
    }

    /**
     * This method verify if the expected data type is valid, if it is not valid then trying to
     * deduct the data type from information into the bson element.
     * @param expectedType Expected data type.
     * @param element Bson element.
     * @return Valid data type.
     */
    default Class typeFromBson(Class expectedType, BsonElement element) {
        Class result = expectedType;

        if(result == null || result.equals(Object.class)) {
            if(element instanceof BsonArray) {
                result = Collection.class;
            } else if(element instanceof BsonDocument) {
                if(((BsonDocument)element).hasElement(PARCELABLE_CLASS_NAME)) {
                    try {
                        result = Class.forName(((BsonDocument) element).get(PARCELABLE_CLASS_NAME).getAsString());
                    } catch(Exception ex){
                        result = Map.class;
                    }
                } else {
                    result = Map.class;
                }
            } else {
                BsonType type = ((BsonPrimitive)element).getType();
                if(type.equals(BsonType.DATE)) {
                    result = Date.class;
                } else if(type.equals(BsonType.BOOLEAN)) {
                    result = Boolean.class;
                } else if(type.equals(BsonType.DOUBLE)) {
                    result = Double.class;
                } else if(type.equals(BsonType.INTEGER)) {
                    result = Integer.class;
                } else if(type.equals(BsonType.LONG)) {
                    result = Long.class;
                } else if(type.equals(BsonType.STRING)) {
                    result = String.class;
                } else if(type.equals(BsonType.BINARY)) {
                    BsonBinarySubType subType = ((BsonPrimitive)element).getBinarySubType();
                    if(subType.equals(BsonBinarySubType.UUID)) {
                        result = UUID.class;
                    } else if(subType.equals(BsonBinarySubType.GENERIC)) {
                        result = byte[].class;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Internal class to create and populate the instance serialized into the bson document.
     */
    final class Builder {

        public static <P extends BsonParcelable> P create(BsonDocument document) {
            P result;
            String className = document.get(PARCELABLE_CLASS_NAME).getAsString();
            try {
                try {
                    result = (P) Class.forName(className).getConstructor().newInstance();
                } catch (Exception e) {
                    try {
                        BsonCustomBuilderLayer bsonCustomBuilderLayer = Layers.get(BsonCustomBuilderLayer.class, className);
                        result = (P) bsonCustomBuilderLayer.create(document);
                    } catch (Exception ex) {
                        result = (P) new BsonParcelableMap();
                    }
                }
                result.populate(document);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to create parcelable instance: " + className, ex);
            }
            return result;
        }

    }
}
