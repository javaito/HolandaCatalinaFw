package org.hcjf.layers.query;

/**
 * Compare two object and return true if the objects are equals and false in other ways.
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Equals extends FieldEvaluator {

    public Equals(String fieldName, Object value) {
        super(new Query.QueryField(fieldName), value);
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
    public boolean evaluate(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters) {
        boolean result;
        try {
            result = getValue(dataSource, consumer, parameters).equals(consumer.get(object, getQueryField().toString()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Equals evaluator fail", ex);
        }
        return result;
    }
}
