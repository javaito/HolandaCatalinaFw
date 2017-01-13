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
    public boolean evaluate(Object object, Query.Consumer consumer) {
        boolean result = false;

        try {
            Object fieldValue = consumer.get(object, getFieldName());
            if(Map.class.isAssignableFrom(fieldValue.getClass())) {
                result = ((Map)fieldValue).containsKey(getValue());
            } else if(Collection.class.isAssignableFrom(fieldValue.getClass())) {
                result = ((Collection)fieldValue).contains(getValue());
            } else if(fieldValue.getClass().isArray()) {
                result = Arrays.binarySearch((Object[])fieldValue, getValue()) >= 0;
            } else if (Map.class.isAssignableFrom(getValue().getClass())) {
                result = ((Map)getValue()).containsKey(fieldValue);
            } else if(Collection.class.isAssignableFrom(getValue().getClass())) {
                result = ((Collection)getValue()).contains(fieldValue);
            } else if(getValue().getClass().isArray()) {
                result = Arrays.binarySearch((Object[])getValue(), fieldValue) >= 0;
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("In evaluator fail", ex);
        }

        return result;
    }
}
