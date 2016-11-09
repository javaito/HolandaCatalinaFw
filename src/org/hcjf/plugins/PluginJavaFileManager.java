package org.hcjf.plugins;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class PluginJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private Map<String, PluginClassCompiled> compiledCode;
    private PluginClassLoader pluginClassLoader;

    public PluginJavaFileManager(JavaFileManager fileManager,
                                    PluginClassLoader pluginClassLoader) {
        super(fileManager);
        this.pluginClassLoader = pluginClassLoader;
        this.compiledCode = new HashMap<>();
    }

    public final void addCode(PluginClassCompiled pluginClassCompiled) {
        compiledCode.put(pluginClassCompiled.getName(), pluginClassCompiled);
        this.pluginClassLoader.addCode(pluginClassCompiled);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location,
                                               String className, JavaFileObject.Kind kind,
                                               FileObject sibling) throws IOException {
        return compiledCode.get(className);
    }

    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        return pluginClassLoader;
    }

}
