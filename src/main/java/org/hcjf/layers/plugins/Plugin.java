package org.hcjf.layers.plugins;

import org.hcjf.utils.Version;

import java.nio.ByteBuffer;

/**
 * @author javaito
 */
public class Plugin {

    private final String groupName;
    private final String name;
    private final Version version;
    private final ByteBuffer jarBuffer;

    public Plugin(String groupName, String name, Version version, ByteBuffer jarBuffer) {
        this.groupName = groupName;
        this.name = name;
        this.version = version;
        this.jarBuffer = jarBuffer;
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
     * Return the in-memory jar file.
     * @return In-memory jar file.
     */
    public ByteBuffer getJarBuffer() {
        return jarBuffer;
    }

    @Override
    public String toString() {
        return groupName + ", " + name + ", " + version.toString();
    }
}
