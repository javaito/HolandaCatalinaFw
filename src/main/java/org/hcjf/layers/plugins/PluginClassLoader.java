package org.hcjf.layers.plugins;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * This class loader is a wrapper of url class loader.
 * @author javaito
 */
public class PluginClassLoader extends URLClassLoader {

    private final Plugin plugin;

    public PluginClassLoader(Plugin plugin, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.plugin = plugin;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG),
                "Unloading plugin: %s", plugin.toString());
    }
}
