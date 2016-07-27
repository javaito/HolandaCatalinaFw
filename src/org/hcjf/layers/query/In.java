package org.hcjf.layers.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class In extends Evaluator {

    public In(String fieldName, Object value) {
        super(fieldName, value);
    }

    @Override
    protected boolean evaluate(Object object, Query.Consumer consumer) {
        boolean result = false;

        try {
            Object fieldValue = consumer.get(object, getFieldName());
            if(Map.class.isAssignableFrom(fieldValue.getClass())) {
                result = ((Map)fieldValue).containsKey(getValue());
            } else if(Collection.class.isAssignableFrom(fieldValue.getClass())) {
                result = ((Collection)fieldValue).contains(getValue());
            } else if(fieldValue.getClass().isArray()) {
                result = Arrays.binarySearch((Object[])fieldValue, getValue()) >= 0;
            } else if(String.class.isAssignableFrom(fieldValue.getClass())) {
                result = ((String)fieldValue).contains(getValue().toString());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("In evaluator fail", ex);
        }

        return result;
    }
}
