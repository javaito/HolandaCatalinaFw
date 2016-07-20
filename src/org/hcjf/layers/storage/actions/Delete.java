package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Delete storage operation.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Delete extends StorageAction {

    public Delete(StorageSession session, String storageName) {
        super(session, storageName);
    }

}
