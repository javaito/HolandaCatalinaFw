package org.hcjf.io.net.broadcast;

import org.hcjf.service.ServiceConsumer;

/**
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
     * Returns the base port to initialize the broadcast server.
     * @return Base port.
     */
    public Integer getBasePort();
}
