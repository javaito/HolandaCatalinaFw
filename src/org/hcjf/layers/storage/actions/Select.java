package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Select storage operation.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Select extends StorageAction {

    private Class resultType;

    public Select(StorageSession session, String storageName) {
        super(session, storageName);
    }

    /**
     * Return the result type that must complete the action with the stored data.
     * @return Result type.
     */
    protected final Class getResultType() {
        return resultType;
    }

    /**
     * Set the result type that must complete the action with the stored data.
     * @param resultType Result type.
     */
    public final void setResultType(Class resultType) {
        this.resultType = resultType;
    }
}
