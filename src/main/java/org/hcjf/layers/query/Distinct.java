package org.hcjf.layers.query;

import java.util.Map;

/**
 * Compare two object and return true if the objects are distinct and false in other ways.
 * @author javaito
 *
 */
public class Distinct extends Equals {

    public Distinct(String fieldName, Object value) {
        super(fieldName, value);
    }

    public Distinct(Query.QueryParameter parameter, Object value) {
        super(parameter, value);
    }

    /**
     * Evaluate if the evaluator's value and the object's value in the specified field of
     * the parameter instance are distinct.
     * This method support any kind of object like field value and parameter value too.
     * @param object Instance to obtain the field value.
     * @param consumer Data source consumer
     * @param valuesMap Contains the definitive values to evaluate the query.
     * @return True if the two values are distinct and false in other ways
     * @throws IllegalArgumentException If is impossible to get value from instance
     * with introspection.
     */
    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        return !super.evaluate(object, consumer, valuesMap);
    }

}
