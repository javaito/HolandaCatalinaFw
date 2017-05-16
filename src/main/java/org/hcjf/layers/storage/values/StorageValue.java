package org.hcjf.layers.storage.values;

/**
 * @author javaito
 *
 */
public class StorageValue {

    private final Object value;

    public StorageValue(Object value) {
        this.value = value;
    }

    /**
     *
     * @return
     */
    public final Object getValue() {
        return value;
    }

}
