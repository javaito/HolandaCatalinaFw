package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Upsert stored action. This action must update data if the data doesn't exist
 * this action must insert the data.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Upsert extends StorageAction {

    public Upsert(StorageSession session, String storageName) {
        super(session, storageName);
    }

}
