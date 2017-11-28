package org.hcjf.layers.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author javaito
 *
 */
public class In extends FieldEvaluator {

    public In(String fieldName, Object value) {
        this(new Query.QueryField(fieldName), value);
    }

    public In(Query.QueryParameter parameter, Object value) {
        super(parameter, value);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        boolean result = false;

        try {
            Object value = valuesMap.get(this);
            if(value instanceof Query.QueryParameter) {
                value = consumer.get(object, (Query.QueryParameter)value);
            }
            Object fieldValue = consumer.get(object, getQueryParameter());
            if(fieldValue instanceof Number) {
                result = containsNumber((Number) fieldValue, value);
            } else if(value instanceof  Number) {
                result = containsNumber((Number) value, fieldValue);
            } else {
                if (Map.class.isAssignableFrom(fieldValue.getClass())) {
                    for(Object key : ((Map) fieldValue).keySet()) {
                        result = result || Objects.equals(key, value);
                        if(result) {
                            break;
                        }
                    }
                } else if (Collection.class.isAssignableFrom(fieldValue.getClass())) {
                    for(Object collectionValue : ((Collection) fieldValue)) {
                        result = result || Objects.equals(collectionValue, value);
                        if(result) {
                            break;
                        }
                    }
                } else if (fieldValue.getClass().isArray()) {
                    for(Object arrayValue : (Object[])fieldValue) {
                        result = Objects.equals(arrayValue, value);
                        if(result) {
                            break;
                        }
                    }
                } else if (Map.class.isAssignableFrom(value.getClass())) {
                    for(Object key : ((Map) value).keySet()) {
                        result = result || Objects.equals(key, fieldValue);
                        if(result) {
                            break;
                        }
                    }
                } else if (Collection.class.isAssignableFrom(value.getClass())) {
                    for(Object collectionValue : ((Collection) value)) {
                        result = result || Objects.equals(collectionValue, fieldValue);
                        if(result) {
                            break;
                        }
                    }
                } else if (value.getClass().isArray()) {
                    for(Object arrayValue : (Object[])value) {
                        result = Objects.equals(arrayValue, fieldValue);
                        if(result) {
                            break;
                        }
                    }
                } else {
                    result = fieldValue.equals(value);
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("In evaluator fail", ex);
        }

        return result;
    }

    private boolean containsNumber(Number numberValue, Object value) {
        boolean result = false;
        Collection collection = null;
        if(Map.class.isAssignableFrom(value.getClass())) {
            collection = ((Map)value).keySet();
        } else if(Collection.class.isAssignableFrom(value.getClass())) {
            collection = ((Collection)value);
        } else if(value.getClass().isArray()) {
            collection = Arrays.asList((Object[])value);
        } else {
            result = numberEquals(numberValue, value);
        }

        if(collection != null) {
            for(Object object : collection) {
                result = numberEquals(numberValue, object);
                if(result) {
                    break;
                }
            }
        }

        return result;
    }
}
