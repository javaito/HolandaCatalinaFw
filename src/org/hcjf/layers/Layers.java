package org.hcjf.layers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manage all the published layers.
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
     * Get from cache the implementation instance or create an instance.
     * @param layerClass Layer interface class.
     * @param clazz Layer implementation class.
     * @param <L> Expected interface class.
     * @return Return the implementation instance.
     */
    private static synchronized <L extends LayerInterface> L getImplementationInstance(
            Class<? extends L> layerClass, Class<? extends Layer> clazz) {
        L result = null;
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
        return result;
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
                result = getImplementationInstance(layerClass, clazz);
            }
        }

        if(result == null) {
            throw new IllegalArgumentException("Layer implementation not found: "
                    + layerClass + "@" + implName);
        }

        return result;
    }

    /**
     * Return the instance of layer that match.
     * @param matcher Layer matcher.
     * @param <L> Expected layer class.
     * @return Layer instance.
     */
    public static <L extends LayerInterface> L get(LayerMatcher<L> matcher) {
        L result = null;
        if(instance.layerImplementations.containsKey(matcher.getLayerClass())) {
            Map<String, Class<? extends Layer>> layersByName =
                    instance.layerImplementations.get(matcher.getLayerClass());
            for(String implName : layersByName.keySet()) {
                result = getImplementationInstance(
                        matcher.getLayerClass(), layersByName.get(implName));
                if(matcher.match((Layer) result)){
                    break;
                } else {
                    result = null;
                }
            }
        }

        if(result == null) {
            throw new IllegalArgumentException("Layer implementation not found");
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

    /**
     * This interface verify if the layer instance match with some particular
     * filter or not.
     * @param <L> Kind of layer
     */
    public interface LayerMatcher<L extends LayerInterface> {

        default Class<? extends L> getLayerClass() {
            Type genericSuperClass = getClass().getGenericSuperclass();
            Type actualType = ((ParameterizedType) genericSuperClass).
                    getActualTypeArguments()[0];
            return (Class<L>) actualType;
        }

        public boolean match(Layer layer);

    }
}
