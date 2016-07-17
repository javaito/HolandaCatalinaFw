package org.hcjf.layers.query;

import org.hcjf.utils.Introspection;

import java.lang.reflect.InvocationTargetException;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Equals extends Evaluator {

    public Equals(String fieldName, Object value) {
        super(fieldName, value);
    }

    @Override
    protected boolean evaluate(Object object) {
        boolean result = false;
        Introspection.Getter getter = Introspection.getGetters(object.getClass()).get(getFieldName());
        try {
            result = getValue().equals(getter.invoke(object));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }
}
