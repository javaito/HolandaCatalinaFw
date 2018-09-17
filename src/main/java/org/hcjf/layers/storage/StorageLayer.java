package org.hcjf.layers.storage;

import org.hcjf.layers.Layer;

/**
 * Base class to make a storage implementation.
 * @author javaito
 */
public abstract class StorageLayer<S extends StorageSession> extends Layer implements StorageLayerInterface<S> {

    public StorageLayer(String implName, boolean stateful) {
        super(implName, stateful);
    }

    public StorageLayer(String implName) {
        super(implName);
    }

}
