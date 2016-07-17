package org.hcjf.layers.query;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Evaluator {

    private final String fieldName;
    private final Object value;

    public Evaluator(String fieldName, Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if(obj.getClass().equals(getClass())) {
            Evaluator evaluator = (Evaluator) obj;
            result = this.fieldName.equals(evaluator.fieldName) &&
                    this.value.equals(evaluator.value);
        }

        return result;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getClass() + "[" + fieldName + "," + value + "]";
    }

    protected abstract boolean evaluate(Object object);
}
