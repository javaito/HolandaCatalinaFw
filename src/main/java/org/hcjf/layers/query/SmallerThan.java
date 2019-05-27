package org.hcjf.layers.query;

import org.hcjf.errors.HCJFRuntimeException;

/**
 * @author javaito
 *
 */
public class SmallerThan extends FieldEvaluator {

    private final boolean orEquals;

    protected SmallerThan(Object leftValue, Object rightValue, boolean orEquals) {
        super(leftValue, rightValue);
        this.orEquals = orEquals;
    }

    public SmallerThan(Object leftValue, Object rightValue) {
        this(leftValue, rightValue, false);
    }

    @Override
    public boolean evaluate(Object object, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        boolean result;
        try {
            Object leftValue = getProcessedLeftValue(object, dataSource, consumer);
            Object rightValue = getProcessedRightValue(object, dataSource, consumer);

            if(leftValue instanceof Number && rightValue instanceof Number) {
                if(leftValue instanceof Double || leftValue instanceof Float ||
                        rightValue instanceof Double || rightValue instanceof Float) {
                    leftValue = Double.valueOf(((Number)leftValue).doubleValue());
                    rightValue = Double.valueOf(((Number)rightValue).doubleValue());
                } else {
                    leftValue = Long.valueOf(((Number)leftValue).longValue());
                    rightValue = Long.valueOf(((Number)rightValue).longValue());
                }
            }

            if(Comparable.class.isAssignableFrom(leftValue.getClass()) &&
                    Comparable.class.isAssignableFrom(rightValue.getClass())) {
                if(leftValue.getClass().isAssignableFrom(rightValue.getClass()) ||
                        rightValue.getClass().isAssignableFrom(leftValue.getClass())) {
                    if(orEquals) {
                        result = ((Comparable)leftValue).compareTo(rightValue) <= 0;
                    } else {
                        result = ((Comparable)leftValue).compareTo(rightValue) < 0;
                    }
                } else {
                    throw new HCJFRuntimeException("Incompatible types between values and field's value: %s != %s", leftValue.getClass(), rightValue.getClass());
                }
            } else {
                throw new HCJFRuntimeException("Unsupported evaluator type: [%s, %s]", leftValue.getClass(), rightValue.getClass());
            }
        } catch (Exception ex) {
            throw new HCJFRuntimeException("Smaller than evaluator fail", ex);
        }
        return result;
    }
}
