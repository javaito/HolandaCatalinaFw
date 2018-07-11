package org.hcjf.layers.query;

/**
 * @author javaito
 *
 */
public class NotIn extends In {

    public NotIn(String fieldName, Object value) {
        super(fieldName, value);
    }

    public NotIn(Object leftValue, Object rightValue) {
        super(leftValue, rightValue);
    }

    @Override
    public boolean evaluate(Object object, Query.DataSource dataSource, Query.Consumer consumer) {
        return !super.evaluate(object, dataSource, consumer);
    }
}
