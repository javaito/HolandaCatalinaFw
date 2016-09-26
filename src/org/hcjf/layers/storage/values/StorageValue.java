package org.hcjf.layers.storage.values;

/**
 * Created by javaito on 22/09/16.
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
