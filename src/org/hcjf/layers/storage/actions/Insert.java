package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Insert storage operation.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Insert<S extends StorageSession> extends StorageAction<S> {

    public Insert(S session) {
        super(session);
    }

    @Override
    protected void onAdd(Object object) {
        setResultType(object.getClass());
        setResourceName(object.getClass().getSimpleName().toLowerCase());
    }
}
