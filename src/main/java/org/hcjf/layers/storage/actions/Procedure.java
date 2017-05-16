package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Procedure storage operation.
 * @author javaito
 *
 */
public abstract class Procedure<S extends StorageSession> extends StorageAction<S> {

    public Procedure(S session) {
        super(session);
    }

}
