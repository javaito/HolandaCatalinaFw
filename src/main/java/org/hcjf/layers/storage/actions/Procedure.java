package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Procedure storage operation.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Procedure<S extends StorageSession> extends StorageAction<S> {

    public Procedure(S session) {
        super(session);
    }

}
