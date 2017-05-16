package org.hcjf.layers.query;

/**
 * This interface represents all object capable of being grouped
 * @author javaito.
 *
 */
public interface Groupable {

    /**
     * Return the value that corresponds to the specific field name.
     * @param fieldName Field name.
     * @return Field value.
     */
    public Object get(String fieldName);

    /**
     *
     * @param fieldName
     * @param value
     */
    public Object put(String fieldName, Object value);

    /**
     * Remove all the elements of the groupable object.
     */
    public void clear();

}
