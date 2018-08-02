package org.hcjf.layers.query;

/**
 * @author javaito
 */
public class BooleanEvaluator extends BaseEvaluator {

    private final Object value;

    public BooleanEvaluator(Object value) {
        this.value = value;
    }

    public final Object getValue() {
        return value;
    }

    @Override
    public boolean evaluate(Object object, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        Object processedValue = getProcessedValue(object, getValue(), dataSource, consumer);
        Boolean result = false;
        if (processedValue != null) {
            if (processedValue instanceof Boolean) {
                result = (Boolean) processedValue;
            } else if (boolean.class.equals(processedValue.getClass())) {
                result = (boolean) processedValue;
            } else if (Number.class.isAssignableFrom(processedValue.getClass())) {
                result = ((Number) processedValue).intValue() != 0;
            } else if (processedValue instanceof String) {
                result = ((String)processedValue).equalsIgnoreCase(Boolean.TRUE.toString());
            } else {
                throw new IllegalArgumentException("Value evaluator only supports boolean values or functions that response with boolean values.");
            }
        }
        return result;
    }
}
