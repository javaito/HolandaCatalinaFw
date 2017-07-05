package org.hcjf.io.net.broadcast;

import org.hcjf.service.ServiceConsumer;

import java.util.Map;

/**
 * This interface provides all the methods as needed to register a broadcast consumer.
 * @author javaito
 */
public interface BroadcastConsumer extends ServiceConsumer {

    /**
     * Returns the name of the task.
     * @return Task name.
     */
    public String getTaskName();

    /**
     * Returns the private key for the consumer.
     * @return Private key.
     */
    public String getPrivateKey();

    /**
     * Returns the ip version to
     * @return
     */
    public String getIpVersion();

    /**
     * Returns the name of the net interface to broadcast messages.
     * @return Net interface name.
     */
    public String getNetInterfaceName();

    /**
     * Returns the port to initialize the broadcast server.
     * @return Port.
     */
    public Integer getPort();

    /**
     * Returns the map with the implementation parameters for the broadcast ping message.
     * @return Parameters map.
     */
    public Map<String, Object> getPingParameters();

    /**
     * This method is invoked when the instance receive a broadcast ping message.
     * @param pingMessage Broadcast ping message.
     */
    public void onPing(BroadcastService.PingMessage pingMessage);

    /**
     * This method is invoked when the instance receive a broadcast pong message.
     * @param pongMessage Broadcast pong message.
     */
    public void onPong(BroadcastService.PongMessage pongMessage);

    /**
     * This method is invoked when the instance receive a broadcast shutdown massage.
     * @param shutdownMessage Broadcast shutdown message.
     */
    public void onShutdown(BroadcastService.ShutdownMessage shutdownMessage);
}
