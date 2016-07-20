package org.hcjf.cloud.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import org.hcjf.cloud.CloudServiceImpl;
import org.hcjf.properties.SystemProperties;

import java.util.Map;
import java.util.Queue;

/**
 *
 * @author javaito
 * @mail javaito@gmail.com
 */
public class HazelcastImpl implements CloudServiceImpl {

    public static final String HAZELCAST_IMPL_NETWORK_PORT = "hcjf.hazelcast.impl.network.port";
    public static final String HAZELCAST_IMPL_NETWORK_PORT_AUTOINCREMENT = "hcjf.hazelcast.impl.network.port.autoincrement";
    public static final String HAZELCAST_IMPL_NETWORK_JOIN_MULTICAST_ENABLED = "hcjf.hazelcast.impl.network.join.multicast.enabled";
    public static final String HAZELCAST_IMPL_NETWORK_JOIN_TCPIP_ENABLED = "hcjf.hazelcast.impl.network.join.tcpip.enabled";
    public static final String HAZELCAST_IMPL_NETWORK_JOIN_TCPIP_MEMBERS = "hcjf.hazelcast.impl.network.join.tcpip.members";
    public static final String HAZELCAST_IMPL_NETWORK_JOIN_TCPIP_REQUIRED_MEMBER = "hcjf.hazelcast.impl.network.join.tcpip.required_member";
    public static final String HAZELCAST_IMPL_NETWORK_INTERFACES_ENABLED = "hcjf.hazelcast.impl.network.join.interfaces.enabled";
    public static final String HAZELCAST_IMPL_NETWORK_INTERFACES = "hcjf.hazelcast.impl.network.join.interfaces";
    public static final String HAZELCAST_IMPL_MAP_NAME = "hcjf.hazelcast.impl.map.name";
    public static final String HAZELCAST_IMPL_MAP_BACKUP_COUNT = "hcjf.hazelcast.impl.map.backup.count";
    public static final String HAZELCAST_IMPL_MAP_TIME_TO_LIVE_SECONDS = "hcjf.hazelcast.impl.map.time.to.live.seconds";
    public static final String HAZELCAST_IMPL_MAP_STORE_CLASS = "hcjf.hazelcast.impl.map.store.class";
    public static final String HAZELCAST_IMPL_MAP_STORE_ENABLE = "hcjf.hazelcast.impl.map.store.enable";

    static {
        SystemProperties.putDefaultValue(HAZELCAST_IMPL_NETWORK_PORT, "5900");
        SystemProperties.putDefaultValue(HAZELCAST_IMPL_NETWORK_PORT_AUTOINCREMENT, "false");
        SystemProperties.putDefaultValue(HAZELCAST_IMPL_NETWORK_JOIN_MULTICAST_ENABLED, "true");
        SystemProperties.putDefaultValue(HAZELCAST_IMPL_NETWORK_JOIN_TCPIP_ENABLED, "false");
        SystemProperties.putDefaultValue(HAZELCAST_IMPL_NETWORK_INTERFACES_ENABLED, "false");
    }

    public HazelcastImpl() {

        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        JoinConfig joinConfig = networkConfig.getJoin();
        Boolean multicast = SystemProperties.getBoolean(HAZELCAST_IMPL_NETWORK_JOIN_MULTICAST_ENABLED);
        if(!multicast) {
            TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig();

        }
    }

    /**
     * This method provides an implementation of distributed map. All the nodes
     * on the cluster shares this instance.
     *
     * @param mapName Name of the map.
     * @return Return the instance of the distributed map.
     */
    @Override
    public <K, V> Map<K, V> getMap(String mapName) {
        return null;
    }

    /**
     * This method provides an implementation of distributed queue. All the nodes
     * on the cluster shares this instance.
     *
     * @param queueName Name of the queue.
     * @return Return the instance of the distributed queue.
     */
    @Override
    public <V> Queue<V> getQueue(String queueName) {
        return null;
    }

    /**
     * This method takes a resource an lock this for all the thread around the cluster
     * and this resource has locked for all the thread for execution.
     * This method is blocked until you can get the lock.
     *
     * @param resourceName The name of the resource to lock.
     */
    @Override
    public void lock(String resourceName) {

    }

    /**
     * This method unlocks a previously locked resource.
     *
     * @param resourceName The name of the resource locked.
     */
    @Override
    public void unlock(String resourceName) {

    }

    /**
     * This method replicates the property add operation over the cloud.
     *
     * @param propertyName  Property name.
     * @param propertyValue Property value.
     */
    @Override
    public void setProperty(String propertyName, String propertyValue) {

    }
}
