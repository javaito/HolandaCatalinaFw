package org.hcjf.layers.query;

import org.hcjf.utils.Introspection;

import java.util.regex.Pattern;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Like extends Evaluator {

    public Like(String fieldName, Object value) {
        super(fieldName, value);
    }

    @Override
    protected boolean evaluate(Object object) {
        boolean result = false;

        Introspection.Getter getter = Introspection.getGetters(object.getClass()).get(getFieldName());
        try {
            Object fieldValue = getter.invoke(object);
            if(fieldValue instanceof String) {
                if(getValue() instanceof Pattern) {
                    result = ((Pattern)getValue()).matcher((String)fieldValue).matches();
                } else if(getValue() instanceof String) {
                    String stringFieldValue = (String) fieldValue;
                    String stringValue = (String) getValue();
                    result = stringFieldValue.contains(stringValue);
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("In evaluator fail", ex);
        }

        return result;
    }
}
