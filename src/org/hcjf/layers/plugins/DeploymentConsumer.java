package org.hcjf.layers.plugins;

import org.hcjf.layers.Layers;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceConsumer;
import org.hcjf.utils.Version;

import java.nio.ByteBuffer;

/**
 * This class implements the abstract process of deployment of plugins
 * using different methods to obtain the in-memory jar file.
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public abstract class DeploymentConsumer implements ServiceConsumer {

    public static final DeploymentFilter DEFAULT_FILTER = new DefaultDeploymentFilter();

    public final DeploymentFilter filter;


    public DeploymentConsumer(DeploymentFilter filter) {
        this.filter = filter;
    }

    /**
     * This method must be called for the implementation when some jar file must be
     * loaded into the system memory.
     * @param jarBuffer In-memory jar file.
     */
    protected final void onJarLoad(ByteBuffer jarBuffer) {
        Layers.publishPlugin(jarBuffer, filter);

        if(SystemProperties.getBoolean(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_ENABLED)) {

        }
    }

    /**
     * The implementations of this interface decide if the plugin components must be
     * deployed or not.
     */
    public interface DeploymentFilter {

        /**
         * If this method return false all the plugin is refused.
         * @param pluginGroup Plugin group.
         * @param pluginName Plugin name.
         * @param pluginVersion Plugin version.
         * @return True if the plugin will be deployed or false if the plugin is refused.
         */
        public boolean matchPlugin(String pluginGroup, String pluginName, Version pluginVersion);

    }

    /**
     * This class is the default filter to deployment listener.
     */
    private static class DefaultDeploymentFilter implements DeploymentFilter {

        /**
         * Return every time true.
         * @param pluginGroup Plugin group.
         * @param pluginName Plugin name.
         * @return Return every time true.
         */
        @Override
        public boolean matchPlugin(String pluginGroup, String pluginName, Version pluginVersion) {
            return true;
        }

    }

}
