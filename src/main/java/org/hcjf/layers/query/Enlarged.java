package org.hcjf.layers.query;

import java.util.Set;

/**
 * This interface provides the functionality to extends the instance domain.
 * @author javaito.
 *
 */
public interface Enlarged {

    /**
     * Return the value that corresponds to the specific field name.
     * @param fieldName Field name.
     * @return Field value.
     */
    Object get(String fieldName);

    /**
     * Add a new value to the instance.
     * @param key Name of the value.
     * @param value Value instance.
     * @return Return the value added.
     */
    Object put(String key, Object value);

    /**
     * Returns the key set of the object.
     * @return Key set of the object
     */
    Set<String> keySet();

    /**
     * Clone th enlarged object.
     * @param fields Array of static fields
     * @return Enlarged clone.
     */
    Enlarged clone(String... fields);

    /**
     * Clone the enlarged object without domain information.
     * @return Enlarged clone.
     */
    Enlarged cloneEmpty();

    /**
     * This method remove all the fields that it's not static
     */
    void purge();
}
