package org.hcjf.layers;

import org.hcjf.cloud.Cloud;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.crud.IdentifiableLayerInterface;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.distributed.DistributedLayer;
import org.hcjf.layers.distributed.DistributedLayerInterface;
import org.hcjf.layers.plugins.Plugin;
import org.hcjf.layers.plugins.PluginClassLoader;
import org.hcjf.layers.plugins.PluginLayer;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;
import org.hcjf.layers.resources.Resource;
import org.hcjf.layers.resources.Resourceable;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.security.LazyPermission;
import org.hcjf.service.security.Permission;
import org.hcjf.service.security.SecurityPermissions;
import org.hcjf.utils.NamedUuid;
import org.hcjf.utils.Strings;
import org.hcjf.utils.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
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

        //Publish a read rows layer implementation in order to publish a list af all the layer of the system.
        Layers.publishLayer(SystemLayerReadableImplementation.class);
        Layers.publishLayer(SystemResourceReadableImplementation.class);
    }

    private final Map<Class<? extends Layer>, Object> initialInstances;
    private final Map<Class<? extends LayerInterface>, Map<String, String>> implAlias;
    private final Map<Class<? extends LayerInterface>, Map<String, Class<? extends Layer>>> layerImplementations;
    private final Map<Class<? extends LayerInterface>, String> defaultLayers;
    private final Map<Class<? extends LayerInterface>, Map<String, String>> pluginLayerImplementations;
    private final Map<Class<? extends LayerInterface>, Map<String, LayerInterface>> distributedLayers;
    private final Map<Class<? extends Layer>, LayerInterface> instanceCache;
    private final Map<String, LayerInterface> pluginWrapperCache;
    private final Map<String, Layer> pluginCache;
    private final Set<Resource> resources;
    private final List<Plugin> plugins;

    private Layers() {
        initialInstances = new HashMap<>();
        implAlias = new HashMap<>();
        layerImplementations = new HashMap<>();
        pluginLayerImplementations = new HashMap<>();
        distributedLayers = new HashMap<>();
        defaultLayers = new HashMap<>();
        instanceCache = new HashMap<>();
        pluginWrapperCache = new HashMap<>();
        pluginCache = new HashMap<>();
        resources = new HashSet<>();
        plugins = new ArrayList<>();
    }

    /**
     * Get from cache the implementation instance or create an instance.
     * @param clazz Layer implementation class.
     * @param <L> Expected interface class.
     * @return Return the implementation instance.
     */
    private static synchronized <L extends LayerInterface> L getImplementationInstance(Class<? extends Layer> clazz) {
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
                throw new HCJFRuntimeException("Unable to create layer instance", ex);
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
     * Gets from cache the distributed layer implementation instance or create a new instance
     * for the combination of this class and implementation name.
     * @param layerClass Layer interface to obtain.
     * @param layerName Layer implementation name.
     * @param <L> Expected layer type.
     * @return Returns the implementation of distributed layer.
     */
    private static synchronized <L extends LayerInterface> L getDistributedImplementationInstance(
            Class<? extends L> layerClass, String layerName) {
        L result;
        if(!instance.distributedLayers.containsKey(layerClass)) {
            instance.distributedLayers.put(layerClass, new HashMap<>());
        }

        result = (L) instance.distributedLayers.get(layerClass).get(layerName);
        if(result == null) {
            result = (L) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                    new Class[]{layerClass}, new DistributedLayer(layerName, layerClass));
            instance.distributedLayers.get(layerClass).put(layerName, result);
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

            //If the implementation class is not founded with the specific name then we check
            //if the implementation name is contained into the aliases.
            if(clazz == null && instance.implAlias.get(layerClass).containsKey(implName)) {
                clazz = instance.layerImplementations.get(layerClass).get(
                        instance.implAlias.get(layerClass).get(implName));
            }

            //If the implementation name not mach with any implementation for the specific layer
            //then we check if the specific layer contains a default implementation.
            if(clazz == null && instance.defaultLayers.containsKey(layerClass)) {
                clazz = instance.layerImplementations.get(layerClass).get(
                        instance.defaultLayers.get(layerClass));
            }

            if(clazz != null) {
                result = getImplementationInstance(clazz);
            }
        }

        //If not exists some implementation for the combination of layer and name implementation then
        //going to try with the plugins.
        if(result == null) {
            if (instance.pluginLayerImplementations.containsKey(layerClass)) {
                String className = instance.pluginLayerImplementations.get(layerClass).get(implName);
                if (className != null) {
                    result = getPluginImplementationInstance(layerClass, className);

                    //Register the implementation name into the named uuid singleton
                    if(result instanceof IdentifiableLayerInterface) {
                        NamedUuid.registerName(result.getImplName());
                    }
                }
            }
        }

        //If not exists some implementation or plugin then going to check the distributed layers,
        //if this kind of layers are available.
        if(result == null) {
            if (SystemProperties.getBoolean(SystemProperties.Layer.DISTRIBUTED_LAYER_ENABLED) &&
                    Cloud.isLayerPublished(layerClass, implName)) {
                result = getDistributedImplementationInstance(layerClass, implName);
            }
        }

        if(result == null) {
            throw new HCJFRuntimeException("Layer implementation not found: %s@%s", layerClass, implName);
        }

        return result;
    }

    /**
     * This method returns all the implementation of the specified layer class as parameter
     * that match with the specified matcher as parameter.
     * @param layerClass King of layer founding.
     * @param matcher Matcher instance.
     * @param <L> Expected layer interface type.
     * @return Set with all the implementation of the layer, this set could be empty.
     */
    public static <L extends LayerInterface> Set<L> getAll(Class<? extends L> layerClass, LayerMatcher<L> matcher) {
        return match(layerClass, matcher, false);
    }

    /**
     * This method returns the first implementation of the specified layer class as parameter
     * that match with the specified matcher as parameter.
     * @param layerClass Kind of layer founding.
     * @param matcher Matcher instance.
     * @param <L> Expected layer implementation type.
     * @return First implementation that match.
     * @throws IllegalArgumentException if any implementation of this kind of layer match.
     */
    public static <L extends LayerInterface> L get(Class<? extends L> layerClass, LayerMatcher<L> matcher) {
        Set<L> result = match(layerClass, matcher, true);

        if(result.isEmpty()) {
            throw new HCJFRuntimeException("Layer implementation not found");
        }

        return result.iterator().next();
    }

    /**
     * This method returns all the implementation of the specified layer class as parameter
     * that match with the specified matcher as parameter. If the parameter only first is true then
     * the result set only contains the first implementation that match.
     * @param layerClass Kind of layer founding.
     * @param matcher Matcher instance.
     * @param onlyFirst Flag to indicate that the search stops with the first occurrence
     * @param <L> Expected layer implementation type.
     * @return Set with all the implementation of the layer, this set could be empty.
     */
    private static <L extends LayerInterface> Set<L> match(Class<? extends L> layerClass, LayerMatcher<L> matcher, boolean onlyFirst) {
        Set<L> result = new HashSet<>();
        L layerFounded;
        if(instance.layerImplementations.containsKey(layerClass)) {
            Map<String, Class<? extends Layer>> layersByName =
                    instance.layerImplementations.get(layerClass);
            for(String implName : layersByName.keySet()) {
                layerFounded = getImplementationInstance(layersByName.get(implName));
                if(matcher.match(layerFounded)){
                    result.add(layerFounded);
                    if(onlyFirst) {
                        break;
                    }
                }
            }
        }

        if(result.isEmpty() || !onlyFirst) {
            if (instance.pluginLayerImplementations.containsKey(layerClass)) {
                Map<String, String> layersByName =
                        instance.pluginLayerImplementations.get(layerClass);
                for (String implName : layersByName.keySet()) {
                    layerFounded = getPluginImplementationInstance(
                            layerClass, layersByName.get(implName));
                    if(matcher.match(layerFounded)){
                        result.add(layerFounded);
                        if(onlyFirst) {
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * This method publish the layers in order to be used by anyone
     * that has the credentials to use the layer.
     * @param layerInstance Layer instance.
     * @param <L> Expected layer type.
     * @return Implementation name.
     * @throws IllegalArgumentException If the layer class is null.
     */
    public static synchronized <L extends Layer> String publishLayer(L layerInstance) {
        Class<? extends Layer> layerClass = layerInstance.getClass();

        if(layerClass.isAnonymousClass() && !layerInstance.isStateful()) {
            throw new HCJFRuntimeException("Unable to publish anonymous and stateless class, to publish anonymous class its must by stateful");
        }

        String implName = layerInstance.getImplName();
        if(implName == null) {
            throw new HCJFRuntimeException("Unable to publish %s because the implementation is not name declared", layerClass);
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

            if(SystemProperties.getBoolean(SystemProperties.Layer.DISTRIBUTED_LAYER_ENABLED)) {
                if (layerInstance instanceof DistributedLayerInterface) {
                    Cloud.publishDistributedLayer(layerInterfaceClass, implName);
                }
            }
        }

        if(layerClass.isAnnotationPresent(DefaultLayer.class)) {
            List<Class> classInterfaces = Arrays.asList(layerClass.getInterfaces());
            for(Class<? extends LayerInterface> defaultInterface : layerClass.getAnnotation(DefaultLayer.class).value()) {
                if(classInterfaces.contains(defaultInterface)){
                    instance.defaultLayers.put(defaultInterface, implName);
                } else {
                    Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG),
                            "The class '%s' could not be a default layer for interface '%s' because " +
                                    "the class don't implements this interface",
                            layerClass, defaultInterface);
                }
            }
        }

        //Register the implementation name into the named uuid singleton
        if(layerInstance instanceof IdentifiableLayerInterface) {
            NamedUuid.registerName(layerInstance.getImplName());
        }

        Class classToIntrospect = layerInstance.getClass();
        while(!classToIntrospect.equals(Layer.class) && !classToIntrospect.equals(Object.class)) {
            for (Method method : classToIntrospect.getDeclaredMethods()) {
                for (Permission permission : method.getDeclaredAnnotationsByType(Permission.class)) {
                    SecurityPermissions.publishPermission(layerInstance.getClass(), permission.value());
                }
                for (LazyPermission permission : method.getDeclaredAnnotationsByType(LazyPermission.class)) {
                    SecurityPermissions.publishPermission(layerInstance.getClass(), permission.value());
                }
            }
            classToIntrospect = classToIntrospect.getSuperclass();
        }

        return implName;
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
            throw new HCJFRuntimeException("Unable to publish a null class");
        }

        Layer layerInstance;
        try {
            layerInstance = layerClass.getConstructor().newInstance();
        } catch(Exception ex){
            throw new HCJFRuntimeException("Unable to publish %s because fail to create a new instance", ex, layerClass);
        }

        return publishLayer(layerInstance);
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
                throw new HCJFRuntimeException("This implementation %s is not over-writable", initialImplementation.getClass().toString());
            }
        }
    }

    /**
     * This method publish all the layer into the plugin jar.
     * @param jarBuffer Plugin jar.
     * @return Plugin instance.
     */
    public static synchronized Plugin publishPlugin(ByteBuffer jarBuffer) {
        String pluginGroupName = Strings.EMPTY_STRING;
        String pluginName = Strings.EMPTY_STRING;
        Version pluginVersion = null;
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
                throw new HCJFRuntimeException("Plugin group name is not specified into the manifest file (Plugin-Group-Name)");
            }

            pluginName = pluginAttributes.getValue(PLUGIN_NAME);
            if(pluginName == null) {
                throw new HCJFRuntimeException("Plugin name is not specified into the manifest file (Plugin-Name)");
            }

            pluginVersion = Version.build(pluginAttributes.getValue(PLUGIN_VERSION));

            result = new Plugin(pluginGroupName, pluginName, pluginVersion);

            boolean deployPlugin = false;
            if(instance.plugins.contains(result)) {
                int currentIndex = instance.plugins.indexOf(result);
                if(!instance.plugins.get(currentIndex).getVersion().equals(result.getVersion())) {
                    Plugin currentPlugin = instance.plugins.remove(currentIndex);
                    if(currentPlugin.getVersion().compareTo(result.getVersion()) > 0) {
                        Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Downgrade plugin version %s.%s:%s -> %s",
                                pluginGroupName, pluginName, currentPlugin.getVersion().toString(), pluginVersion.toString());
                    } else {
                        Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Upgrade plugin version %s.%s:%s -> %s",
                                pluginGroupName, pluginName, currentPlugin.getVersion().toString(), pluginVersion.toString());
                    }
                    deployPlugin = true;
                }
            } else {
                deployPlugin = true;
            }

            if(deployPlugin) {
                instance.plugins.add(result);
                String[] layers = pluginAttributes.getValue(LAYERS).split(CLASS_SEPARATOR);
                Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Deploying plugin %s.%s:%s", pluginGroupName, pluginName, pluginVersion.toString());
                URLClassLoader pluginClassLoader = new PluginClassLoader(result, new URL[]{tempFile.toURI().toURL()},
                        instance.getClass().getClassLoader());

                Class<? extends Layer> layerClass;
                List<Layer> toDeployLayers = new ArrayList<>();
                Layer layer;
                for (String layerClassName : layers) {
                    Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Loading layer %s", layerClassName);
                    layerClass = (Class<? extends Layer>) Class.forName(layerClassName, true, pluginClassLoader);
                    result.addLayer(layerClass);
                    layer = layerClass.getConstructor().newInstance();
                    toDeployLayers.add(layer);
                    Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Layer %s loaded", layer.getImplName());
                }

                for (Layer layerInstance : toDeployLayers) {
                    instance.pluginCache.remove(layerInstance.getClass().getName());
                    instance.pluginCache.put(layerInstance.getClass().getName(), layerInstance);

                    for (Class<? extends LayerInterface> layerInterfaceClass : getLayerInterfaceClass(layerInstance.getClass())) {
                        if (!instance.pluginLayerImplementations.containsKey(layerInterfaceClass)) {
                            instance.pluginLayerImplementations.put(layerInterfaceClass, new HashMap<>());
                        }
                        if (!instance.pluginLayerImplementations.get(layerInterfaceClass).containsKey(layerInstance.getImplName())) {
                            instance.pluginLayerImplementations.get(layerInterfaceClass).put(layerInstance.getImplName(), layerInstance.getClass().getName());
                        }
                    }
                }
            } else {
                Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Exists a plugin deployment in the same group with the same name and version: %s.%s:%s", pluginGroupName, pluginName, pluginVersion.toString());
            }
        } catch (Exception ex) {
            Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Plugin deployment fail %s.%s", ex, pluginGroupName, pluginName);
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
                if (LayerInterface.class.isAssignableFrom(layerInterface) &&
                        !layerInterface.equals(LayerInterface.class)) {
                    result.add(layerInterface);
                }
            }
            introspectedClass = introspectedClass.getSuperclass();
        }

        if(result.isEmpty()) {
            throw new HCJFRuntimeException("Unable to publish %s because must implement a son of LayerClass", layerClass);
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
     * Read all the metadata instance for each published layer.
     * @return Collection with layer metadata.
     */
    private static Collection<JoinableMap> getLayers() {
        Collection<JoinableMap> result = new ArrayList<>();
        JoinableMap map;

        Map<String,Class<? extends Layer>> implementationMap;
        for(Class<? extends LayerInterface> layerInterface : instance.layerImplementations.keySet()) {
            implementationMap = instance.layerImplementations.get(layerInterface);
            for(String name : implementationMap.keySet()) {
                map = new JoinableMap(new HashMap<>());
                map.put("layerInterfaceName", layerInterface.getName());
                map.put("layerImplementationName", implementationMap.get(name).getName());
                map.put("layerName", name);
                map.put("remote", false);
                map.put("plugin", false);
                result.add(map);
            }
        }


        Map<String,String> pluginImplementation;
        for(Class<? extends LayerInterface> layerInterface : instance.pluginLayerImplementations.keySet()) {
            pluginImplementation = instance.pluginLayerImplementations.get(layerInterface);
            for(String name : pluginImplementation.keySet()) {
                map = new JoinableMap(new HashMap<>());
                map.put("layerInterfaceName", layerInterface.getName());
                map.put("layerImplementationName", instance.pluginWrapperCache.get(pluginImplementation.get(name)).getClass().getName());
                map.put("layerName", name);
                map.put("remote", false);
                map.put("plugin", true);
                result.add(map);
            }
        }

        Map<String, LayerInterface> distributeImplementation;
        for(Class<? extends LayerInterface> layerInterface : instance.distributedLayers.keySet()) {
            distributeImplementation = instance.distributedLayers.get(layerInterface);
            for(String name : distributeImplementation.keySet()) {
                map = new JoinableMap(new HashMap<>());
                map.put("layerInterfaceName", layerInterface.getName());
                map.put("layerImplementationName", distributeImplementation.get(name).getClass().getName());
                map.put("layerName", name);
                map.put("remote", true);
                map.put("plugin", false);
                result.add(map);
            }
        }

        return result;
    }

    private static Collection<JoinableMap> getReadableLayers() {
        Collection<JoinableMap> result = new ArrayList<>();
        for(ReadRowsLayerInterface layer : getAll(ReadRowsLayerInterface.class, (L)->true)) {
            result.add(new JoinableMap(Map.of("name", layer.getImplName())));
        }
        return result;
    }

    /**
     * This interface verify if the layer instance match with some particular
     * match or not.
     * @param <L> Kind of layer
     */
    public interface LayerMatcher<L extends LayerInterface> {

        public boolean match(L layer);

    }

    public interface ResourcePredicate extends Predicate<Resource> {}

    public static class SystemLayerReadableImplementation extends Layer implements ReadRowsLayerInterface {

        public SystemLayerReadableImplementation() {
            super(SystemProperties.get(SystemProperties.Layer.READABLE_ALL_LAYER_IMPLEMENTATION_NAME));
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            return queryable.evaluate(getLayers());
        }

    }

    public static class SystemResourceReadableImplementation extends Layer implements ReadRowsLayerInterface {

        public SystemResourceReadableImplementation() {
            super(SystemProperties.get(SystemProperties.Layer.READABLE_LAYER_IMPLEMENTATION_NAME));
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            return queryable.evaluate(getReadableLayers());
        }
    }
}
