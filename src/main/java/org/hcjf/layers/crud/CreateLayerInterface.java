package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;

import java.util.Map;

/**
 * @author javaito
 */
public interface CreateLayerInterface<O extends Object> extends LayerInterface {

    /**
     * This method implements the creation of the resource.
     * @param object Object to represents an instance of the resource.
     * @param parameters Some parameters that could be necessaries
     *                   in order to create an instance of the resource.
     * @return The instance of the resource.
     */
    default O create(O object, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the creation of the resource.
     * @param object Object to represents an instance of the resource.
     * @return The instance of the resource.
     */
    default O create(O object) {
        throw new UnsupportedOperationException();
    }

}
