package org.hcjf.layers.query;

import java.math.BigDecimal;
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
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        boolean result = false;

        try {
            Object value = valuesMap.get(this);
            if(value instanceof Query.QueryParameter) {
                value = consumer.get(object, (Query.QueryParameter)value);
            }
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
                if(fieldValue instanceof Number) {
                    if(value instanceof Number) {
                        if(fieldValue instanceof Double || fieldValue instanceof Float ||
                                value instanceof Double || value instanceof Float) {
                            result = new BigDecimal(((Number) fieldValue).doubleValue()).equals(
                                    new BigDecimal(((Number) value).doubleValue()));
                        } else {
                            result = ((Number) fieldValue).longValue() == ((Number) value).longValue();
                        }
                    } else {
                        result = false;
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
}
