package org.hcjf.layers.resources;

import org.hcjf.layers.LayerInterface;

import java.util.Set;

/**
 * This interface make able that the layer implementation be published as system resource,
 * indexed by an specific name with all the properties needed to consume the resource.
 * @author javaito.
 */
public interface Resourceable {

    /**
     * The implementation of this method must create an instance of resource for each
     * layer interface as parameter.
     * @param layerInterface Layer interface
     * @return Returns the
     */
    public Set<Resource> createResource(Class<? extends LayerInterface> layerInterface);

}
