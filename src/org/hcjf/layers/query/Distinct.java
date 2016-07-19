package org.hcjf.layers.query;

/**
 * Compare two object and return true if the objects are distinct and false in other ways.
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Distinct extends Equals {

    public Distinct(String fieldName, Object value) {
        super(fieldName, value);
    }

    /**
     * Evaluate if the evaluator's value and the object's value in the specified field of
     * the parameter instance are distinct.
     * This method support any kind of object like field value and parameter value too.
     * @param object Instance to obtain the field value.
     * @return True if the two values are distinct and false in other ways
     * @throws IllegalArgumentException If is impossible to get value from instance
     * with introspection.
     */
    @Override
    protected boolean evaluate(Object object) {
        return !super.evaluate(object);
    }

}
