package org.hcjf.layers.query;

import org.hcjf.utils.Introspection;

/**
 * Compare two object and return true if the objects are equals and false in other ways.
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Equals extends Evaluator {

    public Equals(String fieldName, Object value) {
        super(fieldName, value);
    }

    /**
     * Evaluate if the evaluator's value and the object's value in the specified field of
     * the parameter instance are equals.
     * This method support any kind of object like field value and parameter value too.
     * @param object Instance to obtain the field value.
     * @return True if the two values are equals and false in other ways
     * @throws IllegalArgumentException If is impossible to get value from instance
     * with introspection.
     */
    @Override
    protected boolean evaluate(Object object) {
        boolean result;
        Introspection.Getter getter = Introspection.getGetters(object.getClass()).get(getFieldName());
        try {
            result = getValue().equals(getter.invoke(object));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Equals evaluator fail", ex);
        }
        return result;
    }
}
