package org.hcjf.layers.query;

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

    protected SmallerThan(String fieldName, Object value, boolean orEquals) {
        this(new Query.QueryField(fieldName), value, orEquals);
    }

    public SmallerThan(Object leftValue, Object rightValue) {
        this(leftValue, rightValue, false);
    }

    public SmallerThan(String fieldName, Object value) {
        this(new Query.QueryField(fieldName), value, false);
    }

    @Override
    public boolean evaluate(Object object, Query.DataSource dataSource, Query.Consumer consumer) {
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
                    throw new IllegalArgumentException("Incompatible types between values and field's value: " +
                            leftValue.getClass() + " != " + rightValue.getClass());
                }
            } else {
                throw new IllegalArgumentException("Unsupported evaluator type: [" +
                        leftValue.getClass() + ", " + rightValue.getClass() + "]");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Smaller than evaluator fail", ex);
        }
        return result;
    }
}
