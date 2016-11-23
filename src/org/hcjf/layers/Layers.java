package org.hcjf.layers;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author javaito
 * @email javaito@gmail.com
 */
public final class Layers {

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
     * Return the layer interface implementation indexed by implName parameter.
     * @param layerClass Layer interface for the expected implementation.
     * @param implName Implementation name.
     * @param <L> Expected interface.
     * @return Interface implementation.
     * @throws IllegalArgumentException If can't create the instance or the implementation
     * does't exist.
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
     * @return Implementation name.
     * @throws IllegalArgumentException
     */
    public static synchronized String publishLayer(Class<? extends Layer> layerClass) {
        if(layerClass == null) {
            throw new IllegalArgumentException("Unable to publish a null class");
        }

        Class<? extends LayerInterface> layerInterfaceClass = null;
        Class introspectedClass = layerClass;
        while(layerInterfaceClass == null && !introspectedClass.equals(Object.class)) {
            for (Class layerInterface : introspectedClass.getInterfaces()) {
                for (Class superInterface : layerInterface.getInterfaces()) {
                    if (LayerInterface.class.isAssignableFrom(superInterface)) {
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

        if(layerInstance.getImplName() == null) {
            throw new IllegalArgumentException("Unable to publish " + layerClass +
                    " because the implementation is not name declared");
        }
        if(!instance.layerImplementations.containsKey(layerInterfaceClass)) {
            instance.layerImplementations.put(layerInterfaceClass, new HashMap<>());
        }
        instance.layerImplementations.get(layerInterfaceClass).put(layerInstance.getImplName(), layerClass);
        return layerInstance.getImplName();
    }
}
