package org.hcjf.layers.storage;

import org.hcjf.layers.LayerInterface;

/**
 * This kind of layers provides a interface with a storage
 * implementation.
 * @author javaito
 *
 */
public interface StorageLayerInterface<S extends StorageSession> extends LayerInterface {

    /**
     * Return a session with the storage implementation.
     * @return Storage implementation.
     */
    S begin();

}
