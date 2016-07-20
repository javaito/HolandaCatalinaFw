package org.hcjf.layers.query;

/**
 * This abstract class define the structure of the evaluating. The evaluator
 * is the implementation of a method to decide if an object is part of the result add
 * of the query or not is.
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

    /**
     * Two evaluators are equals when are instances of the same class,
     * his field names are equals and his values are equals
     * @param obj Object to compare.
     * @return True if the instance is equals than object parameter and
     * false in the other ways.
     */
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

    /**
     * Return the name of the field (pair getter/setter) in the objects of the
     * data collection.
     * @return Name of the field.
     */
    public final String getFieldName() {
        return fieldName;
    }

    /**
     * Return the value to compare with the field's object of the data collection's
     * instance.
     * @return Object value.
     */
    public final Object getValue() {
        return value;
    }

    /**
     * Return the string representation of the evaluator.
     * @return Format: ClassName[fieldName,value]
     */
    @Override
    public String toString() {
        return getClass() + "[" + fieldName + "," + value + "]";
    }

    /**
     * This method must be implemented for each particular implementation
     * to evaluate some details about instances of the data collection.
     * @param object Object of the data collection.
     * @return Return true if the object must be part of the result add or false in the
     * other ways.
     */
    protected abstract boolean evaluate(Object object);
}
