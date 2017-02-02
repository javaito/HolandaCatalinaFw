package org.hcjf.layers.query;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class SmallerThan extends FieldEvaluator {

    private final boolean orEquals;

    protected SmallerThan(String fieldName, Object value, boolean orEquals) {
        super(new Query.QueryField(fieldName), value);
        this.orEquals = orEquals;
    }

    public SmallerThan(String fieldName, Object value) {
        this(fieldName, value, false);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Object... parameters) {
        boolean result;
        try {
            Object value = getValue(parameters);
            Object fieldValue = consumer.get(object, getQueryField().toString());
            if(Comparable.class.isAssignableFrom(value.getClass())) {
                if(fieldValue.getClass().isAssignableFrom(value.getClass()) ||
                        value.getClass().isAssignableFrom(fieldValue.getClass())) {
                    if(orEquals) {
                        result = ((Comparable)fieldValue).compareTo(value) <= 0;
                    } else {
                        result = ((Comparable)fieldValue).compareTo(value) < 0;
                    }
                } else {
                    throw new IllegalArgumentException("Incompatible types between value and field value ("
                            + getQueryField().toString() + "): " + value.getClass() + " != " + fieldValue.getClass());
                }
            } else {
                throw new IllegalArgumentException("Unsupported evaluator type: " + value.getClass());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Smaller than evaluator fail", ex);
        }
        return result;
    }
}
