package org.hcjf.layers.query;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class SmallerThan extends FieldEvaluator {

    private final boolean orEquals;

    protected SmallerThan(Query.QueryParameter parameter, Object value, boolean orEquals) {
        super(parameter, value);
        this.orEquals = orEquals;
    }

    protected SmallerThan(String fieldName, Object value, boolean orEquals) {
        this(new Query.QueryField(fieldName), value, orEquals);
    }

    public SmallerThan(String fieldName, Object value) {
        this(new Query.QueryField(fieldName), value, false);
    }

    public SmallerThan(Query.QueryParameter parameter, Object value) {
        this(parameter, value, false);
    }

    @Override
    public boolean evaluate(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters) {
        boolean result;
        try {
            Object value = getValue(object, dataSource, consumer, parameters);
            Object fieldValue = consumer.get(object, getQueryParameter());
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
                            + getQueryParameter().toString() + "): " + value.getClass() + " != " + fieldValue.getClass());
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
