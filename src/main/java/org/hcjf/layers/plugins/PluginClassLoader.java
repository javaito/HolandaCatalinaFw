package org.hcjf.layers.plugins;

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

}
