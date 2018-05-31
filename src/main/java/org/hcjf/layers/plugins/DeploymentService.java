package org.hcjf.layers.plugins;

import org.hcjf.cloud.Cloud;
import org.hcjf.layers.Layers;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceConsumer;
import org.hcjf.utils.Version;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author javaito
 */
public final class DeploymentService extends Service<DeploymentService.DeploymentConsumer> {

    private static final DeploymentService instance;

    static {
        instance = new DeploymentService();
    }

    private final Set<String> localDeploymentPlugins;
    private final Map<String, ByteBuffer> cloudDeploymentPlugins;
    private final DeploymentConsumer.DeploymentFilter filter;
    private final Lock lock;
    private final Condition condition;
    private Future cloudFuture;
    private boolean shuttingDown;

    /**
     * Service constructor.
     * @throws NullPointerException If the name is null.
     */
    private DeploymentService() {
        super(SystemProperties.get(SystemProperties.Layer.Deployment.SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.Layer.Deployment.SERVICE_PRIORITY));
        localDeploymentPlugins = new TreeSet<>();

        if(SystemProperties.getBoolean(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_ENABLED)) {
            cloudDeploymentPlugins = Cloud.getMap(SystemProperties. get(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_MAP_NAME));
        } else {
            cloudDeploymentPlugins = null;
        }

        if(SystemProperties.getBoolean(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_ENABLED)) {
            try {
                filter = (DeploymentConsumer.DeploymentFilter)
                        SystemProperties.getClass(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_FILTER).getConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to create instance of deployment filter");
            }
            lock = Cloud.getLock(SystemProperties.get(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_LOCK_NAME));
            condition = Cloud.getCondition(SystemProperties.get(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_LOCK_CONDITION_NAME), lock);
        } else {
            filter = null;
            lock = null;
            condition = null;
        }

        shuttingDown = false;
    }

    /**
     * Return the instance of the service.
     * @return Deployment service instance.
     */
    public static DeploymentService getInstance() {
        return instance;
    }

    /**
     * Init the service.
     */
    @Override
    protected void init() {
        if(SystemProperties.getBoolean(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_ENABLED)) {
            cloudFuture = fork(() -> {
                try {
                    while (!shuttingDown) {
                        try {
                            lock.lock();
                            for(String pluginName : cloudDeploymentPlugins.keySet()) {
                                if(!localDeploymentPlugins.contains(pluginName)) {
                                    //In this case there are a cloud plugin that is not deployed into
                                    //the local instance.
                                    deploy(cloudDeploymentPlugins.get(pluginName), filter, true);
                                }
                            }

                            condition.await();
                            lock.unlock();
                        } catch (Exception ex){
                        }
                    }
                } finally {
                    try {
                        lock.unlock();
                    } catch (Exception ex){}
                }
            });
        }
    }

    /**
     * Deploy a plugin into the cloud.
     * @param jarBuffer Jar buffer of the plugin.
     * @param filter Deployment filter.
     */
    private synchronized void deploy(ByteBuffer jarBuffer, DeploymentConsumer.DeploymentFilter filter, boolean remote) {
        Plugin plugin = Layers.publishPlugin(jarBuffer, filter);
        localDeploymentPlugins.add(plugin.toString());

        if(!remote && SystemProperties.getBoolean(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_ENABLED)) {
            lock.lock();

            cloudDeploymentPlugins.put(plugin.toString(), jarBuffer);
            condition.signalAll();

            lock.unlock();
        }
    }

    /**
     * Shutdown method of the service.
     * @param stage Shutdown stage.
     */
    @Override
    protected void shutdown(ShutdownStage stage) {
        shuttingDown = true;
        cloudFuture.cancel(true);
    }

    /**
     * Register deployment consumer.
     * @param consumer Object with the logic to consume the service.
     */
    @Override
    public void registerConsumer(DeploymentConsumer consumer) {

    }

    /**
     * Unregister deployment consumer.
     * @param consumer Deployment consumer.
     */
    @Override
    public void unregisterConsumer(DeploymentConsumer consumer) {

    }

    /**
     * This class implements the abstract process of deployment of plugins
     * using different methods to obtain the in-memory jar file.
     * @author javaito.
     *
     */
    public abstract static class DeploymentConsumer implements ServiceConsumer {

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
            boolean matchPlugin(String pluginGroup, String pluginName, Version pluginVersion);

        }

    }

    /**
     * This class is the default filter to deployment listener.
     */
    private static class DefaultDeploymentFilter implements DeploymentConsumer.DeploymentFilter {

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
