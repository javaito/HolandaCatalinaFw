package org.hcjf.cloud.impl.network;

import org.hcjf.io.net.broadcast.BroadcastConsumer;
import org.hcjf.io.net.broadcast.BroadcastService;
import org.hcjf.properties.SystemProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 */
public class CloudBroadcastConsumer implements BroadcastConsumer {

    private final Map<String, Object> parameters;

    public CloudBroadcastConsumer() {
        parameters = new HashMap<>();
        parameters.put(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_ADDRESS,
                SystemProperties.get(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_ADDRESS));
        parameters.put(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_PORT,
                SystemProperties.get(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_PORT));
    }

    @Override
    public String getTaskName() {
        return SystemProperties.get(SystemProperties.Cloud.DefaultImpl.Broadcast.TASK_NAME);
    }

    @Override
    public String getPrivateKey() {
        return SystemProperties.get(SystemProperties.Cloud.DefaultImpl.CLUSTER_NAME);
    }

    @Override
    public String getIpVersion() {
        return SystemProperties.get(SystemProperties.Cloud.DefaultImpl.Broadcast.IP_VERSION);
    }

    @Override
    public String getNetInterfaceName() {
        return SystemProperties.get(SystemProperties.Cloud.DefaultImpl.Broadcast.INTERFACE_NAME);
    }

    @Override
    public Integer getPort() {
        return SystemProperties.getInteger(SystemProperties.Cloud.DefaultImpl.Broadcast.PORT);
    }

    @Override
    public Map<String, Object> getPingParameters() {
        return parameters;
    }

    @Override
    public void onPing(BroadcastService.PingMessage pingMessage) {
        String remoteHost = (String) pingMessage.getCustomParameters().get(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_ADDRESS);
        Integer remotePort = (Integer) pingMessage.getCustomParameters().get(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_PORT);
        Node node = new Node();
        node.setLanAddress(remoteHost);
        node.setLanPort(remotePort);
        CloudImpl.getInstance().registerConsumer(node);
    }

    @Override
    public void onPong(BroadcastService.PongMessage pongMessage) {
        String remoteHost = (String) pongMessage.getCustomParameters().get(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_ADDRESS);
        Integer remotePort = (Integer) pongMessage.getCustomParameters().get(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_PORT);
        Node node = new Node();
        node.setLanAddress(remoteHost);
        node.setLanPort(remotePort);
        CloudImpl.getInstance().registerConsumer(node);
    }

    @Override
    public void onShutdown(BroadcastService.ShutdownMessage shutdownMessage) {
    }
}
