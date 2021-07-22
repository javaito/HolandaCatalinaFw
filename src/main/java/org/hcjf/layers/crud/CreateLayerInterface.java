package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;

import java.util.Collection;

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

    /**
     * This method implements the creation of a list of resources.
     * @param objects List of Objects to represents the instances of the resources.
     * @return The list of instances of the resources.
     */
    default Collection<O> create(Collection<O> objects) {
        throw new UnsupportedOperationException();
    }
}
