package org.hcjf.layers.query;

/**
 * @author javaito
 *
 */
public interface Joinable {

    /**
     * Return the value that corresponds to the specific field name.
     * @param fieldName Field name.
     * @return Field value.
     */
    Object get(String fieldName);

    /**
     * Join the data of this joinable instance with the data of the
     * joinable parameter.
     * @param leftResource Name of the left resource of the join.
     * @param rightResource Name of the right resource of the join.
     * @param joinable Joinable parameter.
     * @return Return the current joinable instance.
     */
    Joinable join(String leftResource, String rightResource, Joinable joinable);
}
