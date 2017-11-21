package org.hcjf.layers.query;

import java.util.Map;

/**
 * Compare two object and return true if the objects are equals and false in other ways.
 * @author javaito
 *
 */
public class Equals extends FieldEvaluator {

    public Equals(Query.QueryParameter parameter, Object value) {
        super(parameter, value);
    }

    public Equals(String fieldName, Object value) {
        this(new Query.QueryField(fieldName), value);
    }

    /**
     * Evaluate if the evaluator's value and the object's value in the specified field of
     * the parameter instance are equals.
     * This method support any kind of object like field value and parameter value too.
     * @param object Instance to obtain the field value.
     * @param consumer Data source consumer
     * @param valuesMap Contains the definitive values to evaluate the query.
     * @return True if the two values are equals and false in other ways
     * @throws IllegalArgumentException If is impossible to get value from instance
     * with introspection.
     */
    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        boolean result;
        try {
            Object fieldValue = valuesMap.get(this);
            if(fieldValue instanceof Query.QueryParameter) {
                fieldValue = consumer.get(object, ((Query.QueryParameter)fieldValue));
            }
            Object consumerValue = consumer.get(object, getQueryParameter());
            if(fieldValue instanceof Number) {
                result = numberEquals((Number) fieldValue, consumerValue);
            } else {
                result = fieldValue.equals(consumerValue) || consumerValue.equals(fieldValue);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Equals evaluator fail", ex);
        }
        return result;
    }

}
