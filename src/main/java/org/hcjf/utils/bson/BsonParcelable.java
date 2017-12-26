package org.hcjf.utils.bson;

import org.hcjf.bson.*;
import org.hcjf.utils.Introspection;

import java.util.*;

/**
 * This interface provides the default method to create a bson document of the instance and
 * recreate the instance from bson document.
 * @author javaito.
 */
public interface BsonParcelable {

    /**
     * Returns the bson representation of the instance.
     * @return Bson representation.
     */
    default BsonDocument toBson() {
        BsonDocument document = new BsonDocument();
        document.put(Builder.PARCELABLE_CLASS_NAME, getClass().getName());
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
    default BsonDocument toBson(String name, Map<String, Object> map) {
        BsonDocument document = new BsonDocument(name);
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                document.put(toBson(entry.getKey(), entry.getValue()));
            } catch (Exception ex) {}
        }
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
        if(Collection.class.isAssignableFrom(value.getClass())) {
            result = toBson(name, (Collection)value);
        } else if(Map.class.isAssignableFrom(value.getClass())) {
            result = toBson(name, (Map<String, Object>)value);
        } else if(BsonParcelable.class.isAssignableFrom(value.getClass())) {
            BsonDocument document = ((BsonParcelable)value).toBson();
            document.setName(name);
            result = document;
        } else if(value.getClass().isArray()) {
            result = toBson(name, Arrays.asList(value));
        } else if(value.getClass().isEnum()) {
            result = new BsonPrimitive(name, value.toString());
        } else if(Class.class.equals(value.getClass())) {
            result = new BsonPrimitive(name, ((Class)value).getName());
        } else if(BsonType.fromValue(value) != null) {
            result = new BsonPrimitive(name, value);
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
                        setter.set(this, fromBson(setter.getParameterType(), element));
                    }
                }
            } catch (Exception ex){}
        }

        return (P) this;
    }

    /**
     * Returns a map instance from a bson document.
     * @param document Bson document to create the map instance.
     * @return Map instance.
     */
    default Map<String, Object> fromBson(BsonDocument document) {
        return document.toMap();
    }

    /**
     * Returns a collection instance from a bson array.
     * @param array Bson array to create the collection instance.
     * @return Collection instance.
     */
    default Collection fromBson(BsonArray array) {
        return array.toList();
    }

    /**
     * Creates a instance using the expected data type and bson element.
     * @param expectedDataType Expected result data type.
     * @param element Bson element.
     * @return Object instance.
     */
    default Object fromBson(Class expectedDataType, BsonElement element) {
        Object result;
        if(Collection.class.isAssignableFrom(expectedDataType) && element instanceof BsonArray) {
            result = fromBson((BsonArray)element);
        } else if(Map.class.isAssignableFrom(expectedDataType) && element instanceof BsonDocument) {
            result = fromBson((BsonDocument)element);
        } else if(BsonParcelable.class.isAssignableFrom(expectedDataType) && element instanceof BsonDocument) {
            try {
                BsonParcelable parcelable = (BsonParcelable) expectedDataType.getConstructor().newInstance();
                result = parcelable.populate((BsonDocument) element);
            } catch (Exception ex) {
                throw new IllegalArgumentException();
            }
        } else if(expectedDataType.isEnum() && element instanceof BsonPrimitive) {
            result = Enum.valueOf(expectedDataType, element.getAsString());
        } else if(expectedDataType.equals(Class.class) && element instanceof BsonPrimitive) {
            try {
                result = Class.forName(element.getAsString());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException();
            }
        } else {
            return element.getValue();
        }
        return result;
    }

    final class Builder {

        private static final String PARCELABLE_CLASS_NAME = "__pcn__";

        public static <P extends BsonParcelable> P create(BsonDocument document) {
            String className = document.get(PARCELABLE_CLASS_NAME).getAsString();
            try {
                P result = (P) Class.forName(className).getConstructor().newInstance();
                result.populate(document);
                return result;
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to create parcelable instance: " + className, ex);
            }
        }

    }
}
