package org.hcjf.layers.storage;

import org.hcjf.layers.Layer;

/**
 * Base class to make a storage implementation.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class StorageLayer<S extends StorageSession> extends Layer implements StorageLayerInterface<S> {

    public static final String STORAGE_LOG_TAG = "STORAGE";

    public StorageLayer(String implName, boolean stateful) {
        super(implName, stateful);
    }

    public StorageLayer(String implName) {
        super(implName);
    }
}
