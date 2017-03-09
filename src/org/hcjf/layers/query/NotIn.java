package org.hcjf.layers.query;

import java.util.Map;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class NotIn extends In {

    public NotIn(String fieldName, Object value) {
        super(fieldName, value);
    }

    public NotIn(Query.QueryParameter parameter, Object value) {
        super(parameter, value);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        return !super.evaluate(object, consumer, valuesMap);
    }
}
