package org.hcjf.layers;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Layers {

    private static final Layers instance;

    static {
        instance = new Layers();
    }

    private final Map<Class<? extends LayerInterface>, Map<String, Class<? extends Layer>>> layerImplementations;
    private final Map<Class<? extends Layer>, LayerInterface> instanceCache;

    private Layers() {
        layerImplementations = new HashMap<>();
        instanceCache = new HashMap<>();
    }

    /**
     *
     * @param layerClass
     * @param implName
     * @param <L>
     * @return
     */
    public static <L extends LayerInterface> L get(Class<? extends L> layerClass, String implName) {
        L result = null;
        if(instance.layerImplementations.containsKey(layerClass)) {
            Class<? extends Layer> clazz = instance.layerImplementations.get(layerClass).get(implName);
            if(clazz != null) {
                synchronized (instance) {
                    result = (L) instance.instanceCache.get(clazz);
                    if(result == null) {
                        try {
                            result = (L) clazz.newInstance();

                            result = (L) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                                    new Class[]{layerClass}, result);

                            if(result.isStateful()) {
                                instance.instanceCache.put(clazz, result);
                            }
                        }catch (Exception ex){
                            throw new IllegalArgumentException("Unable to create layer instance", ex);
                        }
                    }
                }
            }
        }

        if(result == null) {
            throw new IllegalArgumentException("Layer implementation not found: "
                    + layerClass + "@" + implName);
        }

        return result;
    }

    /**
     * This method publish the layers in order to be used by anyone
     * that has the credentials to use the layer.
     * @param layerClass
     * @throws IllegalArgumentException
     */
    public static synchronized void publishLayer(Class<? extends Layer> layerClass) {
        if(layerClass == null) {
            throw new IllegalArgumentException("Unable to publish a null class");
        }

        Class<? extends LayerInterface> layerInterfaceClass = null;
        Class introspectedClass = layerClass;
        while(layerInterfaceClass == null && !introspectedClass.equals(Object.class)) {
            for (Class layerInterface : introspectedClass.getInterfaces()) {
                for (Class superInterface : layerInterface.getInterfaces()) {
                    if (superInterface.equals(LayerInterface.class)) {
                        layerInterfaceClass = layerInterface;
                        break;
                    }
                    if (layerInterfaceClass != null) {
                        break;
                    }
                }
            }
            introspectedClass = introspectedClass.getSuperclass();
        }

        if(layerInterfaceClass == null) {
            throw new IllegalArgumentException("Unable to publish " + layerClass +
                    " because must implement a son of LayerClass");
        }

        Layer layerInstance;
        try {
            layerInstance = layerClass.newInstance();
        } catch(Exception ex){
            throw new IllegalArgumentException("Unable to publish " + layerClass +
                    " because fail to create a new instance", ex);
        }

        if(!instance.layerImplementations.containsKey(layerInterfaceClass)) {
            instance.layerImplementations.put(layerInterfaceClass, new HashMap<>());
        }
        instance.layerImplementations.get(layerInterfaceClass).put(layerInstance.getImplName(), layerClass);
    }
}
