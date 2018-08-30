package org.hcjf.layers.query;

import java.util.Set;

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
    Object get(String fieldName);

    /**
     * Returns all the labels into the groupable instance.
     * @return Set with all the labels.
     */
    Set<String> keySet();

    /**
     * Group this instance with the parameter instance.
     * @param groupable Other instance to group.
     * @return Return this instance grouped.
     */
    Groupable group(Groupable groupable);

    /**
     * Remove all the elements of the groupable object.
     */
    void clear();

}
