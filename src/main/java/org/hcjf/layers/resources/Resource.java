package org.hcjf.layers.resources;

import org.hcjf.layers.LayerInterface;
import org.hcjf.utils.Version;

/**
 * @author javaito
 */
public abstract class Resource {

    private final Class<? extends LayerInterface> layerClass;
    private final String resourceName;
    private final Version version;

    public Resource(Class<? extends LayerInterface> layerClass, String resourceName, Version version) {
        this.layerClass = layerClass;
        this.resourceName = resourceName;
        this.version = version;
    }

    /**
     * Returns the layer class that implements the resource.
     * @return Layer class.
     */
    public Class<? extends LayerInterface> getLayerClass() {
        return layerClass;
    }

    /**
     * Returns the name of the resource.
     * @return Name of the resource.
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the version of the resource.
     * @return Version of the resource.
     */
    public Version getVersion() {
        return version;
    }
}
