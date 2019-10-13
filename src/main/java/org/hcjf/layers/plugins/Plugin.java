package org.hcjf.layers.plugins;

import org.hcjf.layers.Layer;
import org.hcjf.utils.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * @author javaito
 */
public final class Plugin {

    private final String groupName;
    private final String name;
    private final Version version;
    private final List<Class<? extends Layer>> layers;

    public Plugin(String groupName, String name, Version version) {
        this.groupName = groupName;
        this.name = name;
        this.version = version;
        this.layers = new ArrayList<>();
    }

    /**
     * Return the group name.
     * @return Group name.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Return the plugin name.
     * @return Plugin name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the plugin version.
     * @return Plugin version.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Add a layer class into the plugin definition.
     * @param layerClass Layer class.
     */
    public void addLayer(Class<? extends Layer> layerClass) {
        layers.add(layerClass);
    }

    /**
     * Returns a list of layer definition.
     * @return Layers list.
     */
    public List<Class<? extends Layer>> getLayers() {
        return layers;
    }

    @Override
    public String toString() {
        return groupName + ", " + name + ", " + version.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if(obj instanceof Plugin) {
            result = groupName.equals(((Plugin)obj).groupName) && name.equals(((Plugin)obj).name);
        }
        return result;
    }
}
