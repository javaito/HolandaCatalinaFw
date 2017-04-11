package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Delete storage operation.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Delete<S extends StorageSession> extends StorageAction<S> {

    public Delete(S session) {
        super(session);
    }

}
