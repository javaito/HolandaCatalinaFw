package org.hcjf.layers.query;

/**
 * Evaluate if the field's value of the instance is greater than the
 * parameter value.
 * @author javaito
 */
public class GreaterThan extends FieldEvaluator {

    private final boolean orEquals;

    protected GreaterThan(Object leftValue, Object rightValue, boolean orEquals) {
        super(leftValue, rightValue);
        this.orEquals = orEquals;
    }

    protected GreaterThan(String fieldName, Object value, boolean orEquals) {
        this(new Query.QueryField(fieldName), value, orEquals);
    }

    public GreaterThan(Object leftValue, Object rightValue) {
        this(leftValue, rightValue, false);
    }

    public GreaterThan(String fieldName, Object value) {
        this(new Query.QueryField(fieldName), value, false);
    }

    /**
     * Evaluate if the field's value of the instance is greater than the
     * parameter value.
     * @param object Object of the data collection.
     * @param dataSource Data source.
     * @param consumer Data source consumer
     * @return True if he field's value is greater than the parameter value and
     * false in the other ways.
     * @throws IllegalArgumentException
     * If the introspection accessor fail: 'Greater than evaluator fail'
     * If the parameter value or field's value are not comparable: 'Unsupported evaluator type'
     * If the parameter value and field's value are incompatible: 'Incompatible types between value and field's value'
     */
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
                        result = ((Comparable)leftValue).compareTo(rightValue) >= 0;
                    } else {
                        result = ((Comparable)leftValue).compareTo(rightValue) > 0;
                    }
                } else {
                    throw new IllegalArgumentException("Incompatible types between values and field's value: " +
                            leftValue.getClass() + " != " + rightValue.getClass());
                }
            } else {
                throw new IllegalArgumentException("Unsupported evaluator type: [" +
                        leftValue.getClass() + ", " + rightValue.getClass() + "]");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Greater than evaluator fail", ex);
        }
        return result;
    }

}
