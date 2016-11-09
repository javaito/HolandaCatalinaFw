package org.hcjf.plugins;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class PluginClassLoader extends ClassLoader {

    private Map<String, PluginClassCompiled> customCompiledCode;

    public PluginClassLoader(ClassLoader parent) {
        super(parent);
        customCompiledCode = new HashMap<>();
    }

    public void addCode(PluginClassCompiled cc) {
        customCompiledCode.put(cc.getName(), cc);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        PluginClassCompiled cc = customCompiledCode.get(name);
        if (cc == null) {
            return super.findClass(name);
        }
        byte[] byteCode = cc.getByteCode();
        return defineClass(name, byteCode, 0, byteCode.length);
    }

}
