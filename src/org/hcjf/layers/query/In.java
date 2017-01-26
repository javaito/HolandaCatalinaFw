package org.hcjf.layers.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class In extends FieldEvaluator {

    public In(String fieldName, Object value) {
        super(fieldName, value);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Object... parameters) {
        boolean result = false;

        try {
            Object value = getValue(parameters);
            Object fieldValue = consumer.get(object, getFieldName());
            if(Map.class.isAssignableFrom(fieldValue.getClass())) {
                result = ((Map)fieldValue).containsKey(value);
            } else if(Collection.class.isAssignableFrom(fieldValue.getClass())) {
                result = ((Collection)fieldValue).contains(value);
            } else if(fieldValue.getClass().isArray()) {
                result = Arrays.binarySearch((Object[])fieldValue, value) >= 0;
            } else if (Map.class.isAssignableFrom(value.getClass())) {
                result = ((Map)value).containsKey(fieldValue);
            } else if(Collection.class.isAssignableFrom(value.getClass())) {
                result = ((Collection)value).contains(fieldValue);
            } else if(value.getClass().isArray()) {
                result = Arrays.binarySearch((Object[])value, fieldValue) >= 0;
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("In evaluator fail", ex);
        }

        return result;
    }
}
