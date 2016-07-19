package org.hcjf.layers.query;

import org.hcjf.utils.Introspection;

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
    protected boolean evaluate(Object object) {
        boolean result = false;

        Introspection.Getter getter = Introspection.getGetters(object.getClass()).get(getFieldName());
        try {
            Object fieldValue = getter.invoke(object);
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
