package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Insert storage operation.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Insert extends StorageAction {

    public Insert(StorageSession session, String storageName) {
        super(session, storageName);
    }

}
