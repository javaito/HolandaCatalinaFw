package org.hcjf.layers.query;

import org.hcjf.errors.HCJFRuntimeException;

/**
 * Compare two object and return true if the objects are equals and false in other ways.
 * @author javaito
 *
 */
public class Equals extends FieldEvaluator {

    public Equals(Object leftValue, Object rightValue) {
        super(leftValue, rightValue);
    }

    /**
     * Evaluate if the evaluator's value and the object's value in the specified field of
     * the parameter instance are equals.
     * This method support any kind of object like field value and parameter value too.
     * @param object Instance to obtain the field value.
     * @param consumer Data source consumer
     * @return True if the two values are equals and false in other ways
     * @throws IllegalArgumentException If is impossible to get value from instance
     * with introspection.
     */
    @Override
    public boolean evaluate(Object object, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        boolean result;
        try {
            Object leftValue = getProcessedLeftValue(object, dataSource, consumer);
            Object rightValue = getProcessedRightValue(object, dataSource, consumer);

            if(leftValue == null) {
                result = rightValue == null;
            } else if(rightValue == null) {
                result = false;
            } else if(leftValue instanceof Number) {
                result = numberEquals((Number) leftValue, rightValue);
            } else if(leftValue.getClass().isEnum() && rightValue.getClass().equals(String.class)) {
                result = leftValue.toString().equals(rightValue);
            } else if(rightValue.getClass().isEnum() && leftValue.getClass().equals(String.class)) {
                result = rightValue.toString().equals(leftValue);
            } else {
                result = leftValue.equals(rightValue) || rightValue.equals(leftValue);
            }
        } catch (Exception ex) {
            throw new HCJFRuntimeException("Equals evaluator fail", ex);
        }
        return result;
    }

}
