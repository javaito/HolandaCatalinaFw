package org.hcjf.layers.plugins;

import org.hcjf.cloud.Cloud;
import org.hcjf.layers.Layers;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public final class DeploymentService extends Service<DeploymentConsumer> {

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
            cloudDeploymentPlugins = Cloud.getMap(SystemProperties.get(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_MAP_NAME));
        } else {
            cloudDeploymentPlugins = null;
        }

        if(SystemProperties.getBoolean(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_ENABLED)) {
            try {
                filter = (DeploymentConsumer.DeploymentFilter)
                        SystemProperties.getClass(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_FILTER).newInstance();
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
     *
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
     *
     * @param jarBuffer
     * @param filter
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
     *
     * @param stage Shutdown stage.
     */
    @Override
    protected void shutdown(ShutdownStage stage) {
        shuttingDown = true;
        cloudFuture.cancel(true);
    }

    /**
     *
     * @param consumer Object with the logic to consume the service.
     */
    @Override
    public void registerConsumer(DeploymentConsumer consumer) {

    }

    /**
     *
     * @param consumer
     */
    @Override
    public void unregisterConsumer(DeploymentConsumer consumer) {

    }
}
