package org.hcjf.layers.query;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class NotIn extends In {

    public NotIn(String fieldName, Object value) {
        super(fieldName, value);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer) {
        return !super.evaluate(object, consumer);
    }
}
