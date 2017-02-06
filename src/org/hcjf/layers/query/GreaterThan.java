package org.hcjf.layers.query;

/**
 * Evaluate if the field's value of the instance is greater than the
 * parameter value.
 * @author javaito
 * @mail javaito@gmail.com
 */
public class GreaterThan extends FieldEvaluator {

    private final boolean orEquals;

    protected GreaterThan(String fieldName, Object value, boolean orEquals) {
        super(new Query.QueryField(fieldName), value);
        this.orEquals = orEquals;
    }

    public GreaterThan(String fieldName, Object value) {
        this(fieldName, value, false);
    }

    /**
     * Evaluate if the field's value of the instance is greater than the
     * parameter value.
     * @param object Object of the data collection.
     * @param consumer Data source consumer
     * @return True if he field's value is greater than the parameter value and
     * false in the other ways.
     * @throws IllegalArgumentException
     * <li> If the introspection accessor fail: 'Greater than evaluator fail' </li>
     * <li> If the parameter value or field's value are not comparable: 'Unsupported evaluator type'</li>
     * <li> If the parameter value and field's valur are incompatible: 'Incompatible types between value and field's value'</li>
     */
    @Override
    public boolean evaluate(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters) {
        boolean result;
        try {
            Object value = getValue(dataSource, consumer, parameters);
            Object fieldValue = consumer.get(object, getQueryField().toString());
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
                            + getQueryField().toString() + "): " + value.getClass() + " != " + fieldValue.getClass());
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
