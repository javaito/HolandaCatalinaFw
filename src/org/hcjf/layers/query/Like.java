package org.hcjf.layers.query;

import java.util.regex.Pattern;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Like extends FieldEvaluator {

    public Like(Query.QueryParameter parameter, Object value) {
        super(parameter, value);
    }

    public Like(String fieldName, Object value) {
        super(new Query.QueryField(fieldName), value);
    }

    @Override
    public boolean evaluate(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters) {
        boolean result = false;

        try {
            Object value = getValue(object, dataSource, consumer, parameters);
            Object fieldValue = consumer.get(object, getQueryParameter());
            if(fieldValue instanceof String) {
                if(value instanceof Pattern) {
                    result = ((Pattern)value).matcher((String)fieldValue).matches();
                } else if(value instanceof String) {
                    String stringFieldValue = (String) fieldValue;
                    String stringValue = (String) value;
                    result = stringFieldValue.contains(stringValue);
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("In evaluator fail", ex);
        }

        return result;
    }
}
