package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Upsert stored action. This action must update data if the data doesn't exist
 * this action must insert the data.
 * @author javaito
 *
 */
public abstract class Upsert<S extends StorageSession> extends StorageAction<S> {

    public Upsert(S session) {
        super(session);
    }

}
