package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Update storage action.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Update extends StorageAction {

    public Update(StorageSession session, String storageName) {
        super(session, storageName);
    }

}
