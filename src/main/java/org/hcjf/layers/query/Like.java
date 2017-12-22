package org.hcjf.layers.query;

import org.hcjf.properties.SystemProperties;

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
                    String wildcard = SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE_WILDCARD);
                    if(stringValue.startsWith(wildcard)) {
                        if(stringValue.endsWith(wildcard)) {
                            result = stringFieldValue.toUpperCase().contains(stringValue.toUpperCase().substring(1,stringValue.length()-1));
                        } else {
                            result = stringFieldValue.toUpperCase().endsWith(stringValue.toUpperCase().substring(1));
                        }
                    } else if(stringValue.endsWith(wildcard)) {
                        result = stringFieldValue.toUpperCase().startsWith(stringValue.toUpperCase().substring(0,stringValue.length()-1));
                    } else {
                        result = stringFieldValue.toUpperCase().contains(stringValue.toUpperCase());
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Like evaluator fail", ex);
        }

        return result;
    }
}
