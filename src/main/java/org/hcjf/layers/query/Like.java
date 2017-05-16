package org.hcjf.layers.query;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author javaito
 *
 */
public class Like extends FieldEvaluator {

    public Like(Query.QueryParameter parameter, Object value) {
        super(parameter, value);
    }

    public Like(String fieldName, Object value) {
        super(new Query.QueryField(fieldName), value);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        boolean result = false;

        try {
            Object value = valuesMap.get(this);
            if(value instanceof Query.QueryParameter) {
                value = consumer.get(object, (Query.QueryParameter)value);
            }
            Object fieldValue = consumer.get(object, getQueryParameter());
            if(fieldValue instanceof String) {
                if(value instanceof Pattern) {
                    result = ((Pattern)value).matcher((String)fieldValue).matches();
                } else if(value instanceof String) {
                    String stringFieldValue = (String) fieldValue;
                    String stringValue = (String) value;
                    result = stringFieldValue.toUpperCase().contains(stringValue.toUpperCase());
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("In evaluator fail", ex);
        }

        return result;
    }
}
