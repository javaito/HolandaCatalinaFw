package org.hcjf.layers.query;

import java.util.Map;

/**
 * Evaluate if the field's value of the instance is greater than the
 * parameter value.
 * @author javaito
 */
public class GreaterThan extends FieldEvaluator {

    private final boolean orEquals;

    protected GreaterThan(Query.QueryParameter parameter, Object value, boolean orEquals) {
        super(parameter, value);
        this.orEquals = orEquals;
    }

    protected GreaterThan(String fieldName, Object value, boolean orEquals) {
        this(new Query.QueryField(fieldName), value, orEquals);
    }

    public GreaterThan(Query.QueryParameter parameter, Object value) {
        this(parameter, value, false);
    }

    public GreaterThan(String fieldName, Object value) {
        this(new Query.QueryField(fieldName), value, false);
    }

    /**
     * Evaluate if the field's value of the instance is greater than the
     * parameter value.
     * @param object Object of the data collection.
     * @param consumer Data source consumer
     * @param valuesMap Values
     * @return True if he field's value is greater than the parameter value and
     * false in the other ways.
     * @throws IllegalArgumentException
     * If the introspection accessor fail: 'Greater than evaluator fail'
     * If the parameter value or field's value are not comparable: 'Unsupported evaluator type'
     * If the parameter value and field's value are incompatible: 'Incompatible types between value and field's value'
     */
    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        boolean result;
        try {
            Object value = valuesMap.get(this);
            if(value instanceof Query.QueryParameter) {
                value = consumer.get(object, ((Query.QueryParameter)value));
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

            if(Comparable.class.isAssignableFrom(value.getClass()) &&
                    Comparable.class.isAssignableFrom(fieldValue.getClass())) {
                if(fieldValue.getClass().isAssignableFrom(value.getClass()) ||
                        value.getClass().isAssignableFrom(fieldValue.getClass())) {
                    if(orEquals) {
                        result = ((Comparable)fieldValue).compareTo(value) >= 0;
                    } else {
                        result = ((Comparable)fieldValue).compareTo(value) > 0;
                    }
                } else {
                    throw new IllegalArgumentException("Incompatible types between value and field's value ("
                            + getQueryParameter().toString() + "): " + value.getClass() + " != " + fieldValue.getClass());
                }
            } else {
                throw new IllegalArgumentException("Unsupported evaluator type: " + value.getClass());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Greater than evaluator fail", ex);
        }
        return result;
    }

}
