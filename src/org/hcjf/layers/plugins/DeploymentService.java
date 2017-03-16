package org.hcjf.layers.plugins;

import org.hcjf.cloud.Cloud;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
        shuttingDown = false;
    }

    @Override
    protected void init() {
        if(SystemProperties.getBoolean(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_ENABLED)) {
            Lock lock = Cloud.getLock(SystemProperties.get(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_LOCK_NAME));
            Condition condition = Cloud.getCondition(SystemProperties.get(SystemProperties.Layer.Deployment.CLOUD_DEPLOYMENT_LOCK_CONDITION_NAME), lock);
            fork(() -> {
                try {
                    while (!shuttingDown) {

                    }
                } finally {
                    try {
                        lock.unlock();
                    } catch (Exception ex){}
                }
            });
        }
    }

    @Override
    protected void shutdown(ShutdownStage stage) {
        super.shutdown(stage);
    }

    /**
     *
     * @param consumer Object with the logic to consume the service.
     */
    @Override
    public void registerConsumer(DeploymentConsumer consumer) {

    }

    @Override
    public void unregisterConsumer(DeploymentConsumer consumer) {

    }
}
