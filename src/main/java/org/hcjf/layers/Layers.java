package org.hcjf.layers;

import org.hcjf.layers.crud.IdentifiableLayerInterface;
import org.hcjf.layers.plugins.DeploymentService;
import org.hcjf.layers.plugins.Plugin;
import org.hcjf.layers.plugins.PluginClassLoader;
import org.hcjf.layers.plugins.PluginLayer;
import org.hcjf.layers.resources.Resource;
import org.hcjf.layers.resources.Resourceable;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.NamedUuid;
import org.hcjf.utils.Strings;
import org.hcjf.utils.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * This class manage all the published layers.
 * @author javaito
 */
public final class Layers {

    private static final String PLUGIN_GROUP_NAME = "Plugin-Group-Name";
    private static final String PLUGIN_NAME = "Plugin-Name";
    private static final String PLUGIN_VERSION = "Plugin-Version";
    private static final String LAYERS = "Layers";
    private static final String CLASS_SEPARATOR = ";";

    private static final Layers instance;

    static {
        instance = new Layers();
    }

    private final Map<Class<? extends Layer>, Object> initialInstances;
    private final Map<Class<? extends LayerInterface>, Map<String, String>> implAlias;
    private final Map<Class<? extends LayerInterface>, Map<String, Class<? extends Layer>>> layerImplementations;
    private final Map<Class<? extends LayerInterface>, Map<String, String>> pluginLayerImplementations;
    private final Map<Class<? extends Layer>, LayerInterface> instanceCache;
    private final Map<String, LayerInterface> pluginWrapperCache;
    private final Map<String, Layer> pluginCache;
    private final Set<Resource> resources;

    private Layers() {
        initialInstances = new HashMap<>();
        implAlias = new HashMap<>();
        layerImplementations = new HashMap<>();
        pluginLayerImplementations = new HashMap<>();
        instanceCache = new HashMap<>();
        pluginWrapperCache = new HashMap<>();
        pluginCache = new HashMap<>();
        resources = new HashSet<>();
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
        if (result == null) {
            try {
                result = (L) instance.initialInstances.get(clazz);
                if(result == null) {
                    result = (L) clazz.getConstructor().newInstance();
                }

                result = (L) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                        getLayerInterfaceClass(clazz).toArray(new Class[]{}), result);

                if (result.isStateful()) {
                    instance.instanceCache.put(clazz, result);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to create layer instance", ex);
            }
        }
        return result;
    }

    /**
     * Get from cache the plugin implementation instance or create a new implementation.
     * @param layerClass Layer interface class.
     * @param layerName Plugin class name.
     * @param <L> Expected interface class.
     * @return Return the plugin implementation instance.
     */
    private static synchronized <L extends LayerInterface> L getPluginImplementationInstance(
            Class<? extends L> layerClass, String layerName) {
        L result = null;
        result = (L) instance.pluginWrapperCache.get(layerName);
        if(result == null) {
            result = (L) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                    new Class[]{layerClass}, new PluginLayer() {
                        @Override
                        protected Object getTarget() {
                            return instance.pluginCache.get(layerName);
                        }
                    });
            instance.pluginWrapperCache.put(layerName, result);
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

        //Check if the implementation name is an alias
        if(instance.layerImplementations.containsKey(layerClass)) {

            Class<? extends Layer> clazz = instance.layerImplementations.get(layerClass).get(implName);
            //If the implementation class is not founded with the specific alias then we check
            //if the implementation name is an alias.
            if(clazz == null && instance.implAlias.get(layerClass).containsKey(implName)) {
                clazz = instance.layerImplementations.get(layerClass).get(
                        instance.implAlias.get(layerClass).get(implName));
            }

            if(clazz != null) {
                result = getImplementationInstance(layerClass, clazz);
            }
        }

        if(result == null) {
            if (instance.pluginLayerImplementations.containsKey(layerClass)) {
                String className = instance.pluginLayerImplementations.get(layerClass).get(implName);
                if (className != null) {
                    result = getPluginImplementationInstance(layerClass, className);
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
     * Return the instance of layer that match.
     * @param layerClass Layer class.
     * @param matcher Layer matcher.
     * @param <L> Expected layer class.
     * @return Layer instance.
     */
    public static <L extends LayerInterface> L get(Class<? extends L> layerClass, LayerMatcher<L> matcher) {
        L result = null;
        if(instance.layerImplementations.containsKey(layerClass)) {
            Map<String, Class<? extends Layer>> layersByName =
                    instance.layerImplementations.get(layerClass);
            for(String implName : layersByName.keySet()) {
                result = getImplementationInstance(
                        layerClass, layersByName.get(implName));
                if(matcher.match(result)){
                    break;
                } else {
                    result = null;
                }
            }
        }

        if(result == null) {
            if (instance.pluginLayerImplementations.containsKey(layerClass)) {
                Map<String, String> layersByName =
                        instance.pluginLayerImplementations.get(layerClass);
                for (String implName : layersByName.keySet()) {
                    result = getPluginImplementationInstance(
                            layerClass, layersByName.get(implName));
                    if(matcher.match(result)){
                        break;
                    } else {
                        result = null;
                    }
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
     * @param layerClass Layer class.
     * @return Implementation name.
     * @throws IllegalArgumentException If the layer class is null.
     */
    public static synchronized String publishLayer(Class<? extends Layer> layerClass) {
        if(layerClass == null) {
            throw new IllegalArgumentException("Unable to publish a null class");
        }

        Layer layerInstance;
        try {
            layerInstance = layerClass.getConstructor().newInstance();
        } catch(Exception ex){
            throw new IllegalArgumentException("Unable to publish " + layerClass +
                    " because fail to create a new instance", ex);
        }

        String implName = layerInstance.getImplName();
        if(implName == null) {
            throw new IllegalArgumentException("Unable to publish " + layerClass +
                    " because the implementation is not name declared");
        }

        for(Class<? extends LayerInterface> layerInterfaceClass : getLayerInterfaceClass(layerClass)) {
            //Creates the map for the implementations and aliases
            if (!instance.layerImplementations.containsKey(layerInterfaceClass)) {
                instance.layerImplementations.put(layerInterfaceClass, new HashMap<>());
                instance.implAlias.put(layerInterfaceClass, new HashMap<>());
            }

            //Check if the impl name exist into the implementations.
            if (instance.layerImplementations.get(layerInterfaceClass).containsKey(implName)) {
                checkOverwriteAlias(layerInterfaceClass, layerInstance, implName);
            }

            //Check if the some alias exist into the map of aliases for the specific interface.
            if (layerInstance.getAliases() != null) {
                for (String alias : layerInstance.getAliases()) {
                    checkOverwriteAlias(layerInterfaceClass, layerInstance, alias);
                }
            }

            if(layerInstance.isStateful()) {
                instance.initialInstances.put(layerClass, layerInstance);
            }
            instance.layerImplementations.get(layerInterfaceClass).put(implName, layerClass);

            //Add one map entry for each alias with the same implementation name.
            if (layerInstance.getAliases() != null) {
                for (String alias : layerInstance.getAliases()) {
                    instance.implAlias.get(layerInterfaceClass).put(alias, implName);
                }
            }

            if(layerInstance instanceof Resourceable) {
                ((Resourceable)layerInstance).createResource(layerInterfaceClass).forEach(
                        R->instance.resources.add(R));
            }
        }

        //Register the implementation name into the named uuid singleton
        if(layerInstance instanceof IdentifiableLayerInterface) {
            NamedUuid.registerName(layerInstance.getImplName());
        }

        return implName;
    }

    /**
     * Check if the implementation could be overwritten.
     * @param layerInterfaceClass Layer interface.
     * @param layerInstance Layer instance.
     */
    private static void checkOverwriteAlias(Class<? extends LayerInterface> layerInterfaceClass, Layer layerInstance, String alias) {
        Layer initialImplementation =
                (Layer) instance.initialInstances.get(
                        instance.layerImplementations.get(layerInterfaceClass).get(layerInstance.getImplName()));
        if(initialImplementation != null) {
            if (initialImplementation.isOverwritable()) {
                Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG),
                        "The alias %s for the instance %s will be overwritten for instance of %s", alias,
                        initialImplementation.getClass().getName(), layerInstance.getClass().getName());
            } else {
                throw new SecurityException("This implementation " + initialImplementation.getClass().toString() + " is not overwritable");
            }
        }
    }

    /**
     * This method publish all the layer into the plugin jar.
     * @param jarBuffer Plugin jar.
     * @return Plugin instance.
     */
    public static synchronized Plugin publishPlugin(ByteBuffer jarBuffer) {
        return publishPlugin(jarBuffer, DeploymentService.DeploymentConsumer.DEFAULT_FILTER);
    }

    /**
     * This method publish all the layer into the plugin jar.
     * @param jarBuffer Plugin jar.
     * @param filter Deployment filter.
     * @return Plugin instance.
     */
    public static synchronized Plugin publishPlugin(ByteBuffer jarBuffer, DeploymentService.DeploymentConsumer.DeploymentFilter filter) {
        String pluginGroupName = Strings.EMPTY_STRING;
        String pluginName = Strings.EMPTY_STRING;
        Plugin result = null;
        try {
            File tempFile = File.createTempFile("."+UUID.randomUUID().toString(), "tmp");

            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(jarBuffer.array());
            fos.flush();
            fos.close();
            JarFile jarFile = new JarFile(tempFile);
            Manifest manifest = jarFile.getManifest();
            Attributes pluginAttributes = manifest.getMainAttributes();

            pluginGroupName = pluginAttributes.getValue(PLUGIN_GROUP_NAME);
            if(pluginGroupName == null) {
                throw new IllegalArgumentException("Plugin group name is not specified into the manifest file (Plugin-Group-Name)");
            }

            pluginName = pluginAttributes.getValue(PLUGIN_NAME);
            if(pluginName == null) {
                throw new IllegalArgumentException("Plugin name is not specified into the manifest file (Plugin-Name)");
            }

            Version pluginVersion = Version.build(pluginAttributes.getValue(PLUGIN_VERSION));

            result = new Plugin(pluginGroupName, pluginName, pluginVersion, jarBuffer);
            if(filter.matchPlugin(pluginGroupName, pluginName, pluginVersion)) {
                String[] layers = pluginAttributes.getValue(LAYERS).split(CLASS_SEPARATOR);
                Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Deploying plugin %s", pluginName);
                URLClassLoader pluginClassLoader = new PluginClassLoader(result, new URL[]{tempFile.toURI().toURL()},
                        instance.getClass().getClassLoader());

                Class<? extends Layer> layerClass;
                List<Layer> toDeployLayers = new ArrayList<>();
                Layer layer;
                for (String layerClassName : layers) {
                    Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Loading layer %s", layerClassName);
                    layerClass = (Class<? extends Layer>) Class.forName(layerClassName, true, pluginClassLoader);
                    getLayerInterfaceClass(layerClass);
                    layer = layerClass.getConstructor().newInstance();
                    toDeployLayers.add(layer);
                    Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Layer %s loaded", layer.getImplName());
                }

                for (Layer layerInstance : toDeployLayers) {
                    instance.pluginCache.remove(layerInstance.getClass().getName());
                    instance.pluginCache.put(layerInstance.getClass().getName(), layerInstance);

                    for(Class<? extends LayerInterface> layerInterfaceClass : getLayerInterfaceClass(layerInstance.getClass())) {
                        if (!instance.pluginLayerImplementations.containsKey(layerInterfaceClass)) {
                            instance.pluginLayerImplementations.put(layerInterfaceClass, new HashMap<>());
                        }
                        if (!instance.pluginLayerImplementations.get(layerInterfaceClass).containsKey(layerInstance.getImplName())) {
                            instance.pluginLayerImplementations.get(layerInterfaceClass).put(layerInstance.getImplName(), layerInstance.getClass().getName());
                        }
                    }
                }
            } else {
                Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Plugin refused (%s:%s)", pluginGroupName, pluginName);
            }
        } catch (Exception ex) {
            Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Plugin deployment fail (%s:%s)", ex, pluginGroupName, pluginName);
        }

        return result;
    }

    /**
     * Return the layer interface that implements the layer class.
     * @param layerClass  Layer class.
     * @return Layer interface implemented.
     */
    public static Set<Class<? extends LayerInterface>> getLayerInterfaceClass(Class<? extends Layer> layerClass) {
        Set<Class<? extends LayerInterface>> result = new HashSet<>();
        Class introspectedClass = layerClass;
        while(!introspectedClass.equals(Object.class)) {
            for (Class layerInterface : introspectedClass.getInterfaces()) {
                for (Class superInterface : layerInterface.getInterfaces()) {
                    if (LayerInterface.class.isAssignableFrom(layerInterface) &&
                            !layerInterface.equals(LayerInterface.class)) {
                        result.add(layerInterface);
                    }
                }
            }
            introspectedClass = introspectedClass.getSuperclass();
        }

        if(result.isEmpty()) {
            throw new IllegalArgumentException("Unable to publish " + layerClass +
                    " because must implement a son of LayerClass");
        }

        return result;
    }

    /**
     * Returns all the resources published into the system.
     * @return Set with all the resources.
     */
    public static Set<Resource> getResources() {
        return getResources(R->true);
    }

    /**
     * Returns the resources that meet the predicate.
     * @param predicate Resource predicate.
     * @return Set with the resources.
     */
    public static Set<Resource> getResources(ResourcePredicate predicate) {
        return instance.resources.stream().filter(predicate).collect(Collectors.toSet());
    }

    /**
     * This interface verify if the layer instance match with some particular
     * filter or not.
     * @param <L> Kind of layer
     */
    public interface LayerMatcher<L extends LayerInterface> {

        public boolean match(L layer);

    }

    public interface ResourcePredicate extends Predicate<Resource> {}
}
