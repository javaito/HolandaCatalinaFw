package org.hcjf.layers.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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
                    result = ((Map) fieldValue).containsKey(value);
                } else if (Collection.class.isAssignableFrom(fieldValue.getClass())) {
                    result = ((Collection) fieldValue).contains(value);
                } else if (fieldValue.getClass().isArray()) {
                    for(Object arrayValue : (Object[])fieldValue) {
                        result = arrayValue.equals(value);
                        if(result) {
                            break;
                        }
                    }
                } else if (Map.class.isAssignableFrom(value.getClass())) {
                    result = ((Map) value).containsKey(fieldValue);
                } else if (Collection.class.isAssignableFrom(value.getClass())) {
                    result = ((Collection) value).contains(fieldValue);
                } else if (value.getClass().isArray()) {
                    for(Object arrayValue : (Object[])value) {
                        result = arrayValue.equals(fieldValue);
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
