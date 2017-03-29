package org.hcjf.layers;

import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.layers.plugins.DeploymentService;
import org.hcjf.layers.plugins.Plugin;
import org.hcjf.layers.plugins.PluginClassLoader;
import org.hcjf.layers.plugins.PluginLayer;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Strings;
import org.hcjf.utils.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This class manage all the published layers.
 * @author javaito
 * @email javaito@gmail.com
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

    private final Map<Class<? extends LayerInterface>, Map<String, Class<? extends Layer>>> layerImplementations;
    private final Map<Class<? extends LayerInterface>, Map<String, String>> pluginLayerImplementations;
    private final Map<Class<? extends Layer>, LayerInterface> instanceCache;
    private final Map<String, LayerInterface> pluginWrapperCache;
    private final Map<String, Layer> pluginCache;

    private Layers() {
        layerImplementations = new HashMap<>();
        pluginLayerImplementations = new HashMap<>();
        instanceCache = new HashMap<>();
        pluginWrapperCache = new HashMap<>();
        pluginCache = new HashMap<>();
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
                result = (L) clazz.newInstance();
                result = (L) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                        new Class[]{layerClass}, result);

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
        if(instance.layerImplementations.containsKey(layerClass)) {
            Class<? extends Layer> clazz = instance.layerImplementations.get(layerClass).get(implName);
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
                if(matcher.match(result)){
                    break;
                } else {
                    result = null;
                }
            }
        }

        if(result == null) {
            if (instance.pluginLayerImplementations.containsKey(matcher.getLayerClass())) {
                Map<String, String> layersByName =
                        instance.pluginLayerImplementations.get(matcher.getLayerClass());
                for (String implName : layersByName.keySet()) {
                    result = getPluginImplementationInstance(
                            matcher.getLayerClass(), layersByName.get(implName));
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
     * @param layerClass
     * @return Implementation name.
     * @throws IllegalArgumentException
     */
    public static synchronized String publishLayer(Class<? extends Layer> layerClass) {
        if(layerClass == null) {
            throw new IllegalArgumentException("Unable to publish a null class");
        }

        Class<? extends LayerInterface> layerInterfaceClass = getLayerInterfaceClass(layerClass);

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
     * This method publish all the layer into the plugin jar.
     * @param jarBuffer Plugin jar.
     */
    public static synchronized Plugin publishPlugin(ByteBuffer jarBuffer) {
        return publishPlugin(jarBuffer, DeploymentService.DeploymentConsumer.DEFAULT_FILTER);
    }

    /**
     * This method publish all the layer into the plugin jar.
     * @param jarBuffer Plugin jar.
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
                Class<? extends LayerInterface> layerInterfaceClass;
                List<Layer> toDeployLayers = new ArrayList<>();
                Layer layer;
                for (String layerClassName : layers) {
                    Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Loading layer %s", layerClassName);
                    layerClass = (Class<? extends Layer>) Class.forName(layerClassName, true, pluginClassLoader);
                    getLayerInterfaceClass(layerClass);
                    layer = layerClass.newInstance();
                    toDeployLayers.add(layer);
                    Log.d(SystemProperties.get(SystemProperties.Layer.LOG_TAG), "Layer %s loaded", layer.getImplName());
                }

                for (Layer layerInstance : toDeployLayers) {
                    layerInterfaceClass = getLayerInterfaceClass(layerInstance.getClass());
                    instance.pluginCache.remove(layerInstance.getClass().getName());
                    instance.pluginCache.put(layerInstance.getClass().getName(), layerInstance);

                    if (!instance.pluginLayerImplementations.containsKey(layerInterfaceClass)) {
                        instance.pluginLayerImplementations.put(layerInterfaceClass, new HashMap<>());
                    }
                    if (!instance.pluginLayerImplementations.get(layerInterfaceClass).containsKey(layerInstance.getImplName())) {
                        instance.pluginLayerImplementations.get(layerInterfaceClass).put(layerInstance.getImplName(), layerInstance.getClass().getName());
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
    public static Class<? extends LayerInterface> getLayerInterfaceClass(Class<? extends Layer> layerClass) {
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

        return layerInterfaceClass;
    }

    /**
     * This interface verify if the layer instance match with some particular
     * filter or not.
     * @param <L> Kind of layer
     */
    public interface LayerMatcher<L extends LayerInterface> {

        default Class<? extends L> getLayerClass() {
            Type actualType = null;
            for(Type genericInterface : getClass().getGenericInterfaces()) {
                if(LayerMatcher.class.isAssignableFrom((Class)genericInterface)) {
                    actualType = ((ParameterizedType) genericInterface).
                            getActualTypeArguments()[0];
                    break;
                }
            }
            return (Class<L>) actualType;
        }

        public boolean match(L layer);

    }

    public static void main(String[] args) throws Exception {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");

        new Thread(() -> {
            while(true) {
                try {
                    System.in.read();
                    File file2 = new File("/home/javaito/IdeaProjects/TestPlugin/out/artifacts/TestPlugin/TestPlugin.jar");
                    ByteBuffer jarBuffer2 = ByteBuffer.wrap(Files.readAllBytes(file2.toPath()));
                    publishPlugin(jarBuffer2);
                } catch (Exception ex){}
            }
        }).start();

        while(true) {
            CrudLayerInterface<String> crudLayerInterface;
            try {
                crudLayerInterface = Layers.get(CrudLayerInterface.class, "MyModelCrud");
            } catch (Exception ex) {
                System.out.println("Plugin not found");
                Thread.sleep(4000);
                continue;
            }
            while (true) {
                try {
                    crudLayerInterface.create("Javaito");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Thread.sleep(4000);
            }
        }

    }
}
