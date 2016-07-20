package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageLayer;
import org.hcjf.log.Log;

/**
 * This class is the base class to implements all the result sets
 * for storage layer.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class ResultSet<O extends Object> {

    private final Integer size;
    private final O result;

    public ResultSet(Integer size, O result) {
        this.size = size;
        this.result = result;
    }

    public Integer getSize() {
        return size;
    }

    public <R extends O> R getResult() {
        try {
            return (R) result;
        } catch (ClassCastException ex){
            Log.e(StorageLayer.STORAGE_LOG_TAG, "Result set contains %s and it expected something else", ex, result.getClass());
            throw ex;
        }
    }
}
