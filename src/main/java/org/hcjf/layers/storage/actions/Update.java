package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Update storage action.
 * @author javaito
 *
 */
public abstract class Update<S extends StorageSession> extends StorageAction<S> {

    public Update(S session) {
        super(session);
    }

}
