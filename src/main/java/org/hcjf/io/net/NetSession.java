package org.hcjf.io.net;

import org.hcjf.service.ServiceSession;

import java.util.UUID;

/**
 * This interface define de behavior of the net session.
 * @author javaito
 */
public abstract class NetSession extends ServiceSession {

    private final NetServiceConsumer consumer;
    private String remoteHost;
    private int remotePort;
    private boolean checked;

    public NetSession(UUID id, NetServiceConsumer consumer) {
        super(id);
        this.consumer = consumer;
    }

    protected NetSession(NetSession netSession) {
        super(netSession);
        this.consumer = netSession.consumer;
        this.remoteHost = netSession.remoteHost;
        this.remotePort = netSession.remotePort;
        this.checked = netSession.checked;
    }

    /**
     * Return the consumer.
     * @return Consumer.
     */
    public NetServiceConsumer getConsumer() {
        return consumer;
    }

    /**
     * Return a value to indicate if the session is checked.
     * @return True if the session is checked and false in otherwise
     */
    public final boolean isChecked() {
        return checked;
    }

    /**
     * Set a value to indicate if the session is chacked.
     * @param checked Checked value.
     */
    public final void setChecked(boolean checked) {
        this.checked = checked;
    }

    /**
     * Returns the remote host of the session.
     * @return Remote host.
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * Sets the remote host of the session.
     * @param remoteHost Remote host.
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    /**
     * Returns the remote port of the session.
     * @return Remote port.
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Sets the remote port of the session.
     * @param remotePort Remote port.
     */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
