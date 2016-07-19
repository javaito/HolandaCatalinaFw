package org.hcjf.layers.query;

import org.hcjf.utils.Introspection;

/**
 * Evaluate if the field's value of the instance is greater than the
 * parameter value.
 * @author javaito
 * @mail javaito@gmail.com
 */
public class GreaterThan extends Evaluator {

    private final boolean orEquals;

    protected GreaterThan(String fieldName, Object value, boolean orEquals) {
        super(fieldName, value);
        this.orEquals = orEquals;
    }

    public GreaterThan(String fieldName, Object value) {
        this(fieldName, value, false);
    }

    /**
     * Evaluate if the field's value of the instance is greater than the
     * parameter value.
     * @param object Object of the data collection.
     * @return True if he field's value is greater than the parameter value and
     * false in the other ways.
     * @throws IllegalArgumentException
     * <li> If the introspection accessor fail: 'Greater than evaluator fail' </li>
     * <li> If the parameter value or field's value are not comparable: 'Unsupported evaluator type'</li>
     * <li> If the parameter value and field's valur are incompatible: 'Incompatible types between value and field's value'</li>
     */
    @Override
    protected boolean evaluate(Object object) {
        boolean result;
        Introspection.Getter getter = Introspection.getGetters(object.getClass()).get(getFieldName());
        try {
            Object fieldValue = getter.invoke(object);
            if(Comparable.class.isAssignableFrom(getValue().getClass()) &&
                    Comparable.class.isAssignableFrom(fieldValue.getClass())) {
                if(fieldValue.getClass().isAssignableFrom(getValue().getClass()) ||
                        getValue().getClass().isAssignableFrom(fieldValue.getClass())) {
                    if(orEquals) {
                        result = ((Comparable)fieldValue).compareTo(getValue()) >= 0;
                    } else {
                        result = ((Comparable)fieldValue).compareTo(getValue()) > 0;
                    }
                } else {
                    throw new IllegalArgumentException("Incompatible types between value and field's value ("
                            + getFieldName() + "): " + getValue().getClass() + " != " + fieldValue.getClass());
                }
            } else {
                throw new IllegalArgumentException("Unsupported evaluator type: " + getValue().getClass());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Greater than evaluator fail", ex);
        }
        return result;
    }


}
