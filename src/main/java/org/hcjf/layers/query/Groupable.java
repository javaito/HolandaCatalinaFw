package org.hcjf.layers.query;

/**
 * This interface represents all object capable of being grouped
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public interface Groupable {

    /**
     * Return the value that corresponds to the specific field name.
     * @param fieldName Field name.
     * @return Field value.
     */
    public Object get(String fieldName);

    /**
     * Group the current instance of the groupable instance with other instance.
     * @param groupable Other instance.
     */
    public void group(Groupable groupable);

}
