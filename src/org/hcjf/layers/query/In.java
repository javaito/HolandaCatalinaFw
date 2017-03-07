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
        this(new Query.QueryField(fieldName), value);
    }

    public In(Query.QueryParameter parameter, Object value) {
        super(parameter, value);
    }

    @Override
    public boolean evaluate(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters) {
        boolean result = false;

        try {
            Object value = getValue(object, dataSource, consumer, parameters);
            Object fieldValue = consumer.get(object, getQueryParameter());
            if(fieldValue instanceof Number) {
                if(fieldValue instanceof Double || fieldValue instanceof Float) {
                    fieldValue = new Double(((Number)fieldValue).doubleValue());
                } else {
                    fieldValue = new Long(((Number)fieldValue).longValue());
                }
            }
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
            } else {
                //If the field value is not any kind of collection then the method evaluate equals condition between
                //the field value and the value.
                result = value.equals(fieldValue);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("In evaluator fail", ex);
        }

        return result;
    }
}
