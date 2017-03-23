package org.hcjf.layers.query;

import java.util.Map;

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
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        boolean result;
        try {
            Object value = valuesMap.get(this);
            if(value instanceof Query.QueryParameter) {
                value = consumer.get(object, (Query.QueryParameter)value);
            }
            Object fieldValue = consumer.get(object, getQueryParameter());

            if(fieldValue instanceof Number && value instanceof Number) {
                if(fieldValue instanceof Double || fieldValue instanceof Float ||
                        value instanceof Double || value instanceof Float) {
                    fieldValue = new Double(((Number)fieldValue).doubleValue());
                    value = new Double(((Number)value).doubleValue());
                } else {
                    fieldValue = new Long(((Number)fieldValue).longValue());
                    value = new Long(((Number)value).longValue());
                }
            }

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
