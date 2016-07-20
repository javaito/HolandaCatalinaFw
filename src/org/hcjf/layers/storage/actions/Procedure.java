package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

/**
 * Procedure storage operation.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Procedure extends StorageAction {

    public Procedure(StorageSession session, String storageName) {
        super(session, storageName);
    }

}
