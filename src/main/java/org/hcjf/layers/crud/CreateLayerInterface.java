package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;

/**
 * @author javaito
 */
public interface CreateLayerInterface<O extends Object> extends LayerInterface {

    /**
     * This method implements the creation of the resource.
     * @param object Object to represents an instance of the resource.
     * @return The instance of the resource.
     */
    default O create(O object) {
        throw new UnsupportedOperationException();
    }

}
