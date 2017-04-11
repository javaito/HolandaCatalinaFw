package org.hcjf.layers.storage.values;

/**
 * @author javaito
 * @email javaito@gmail.com
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
