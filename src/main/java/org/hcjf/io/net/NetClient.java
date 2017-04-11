package org.hcjf.io.net;

/**
 * This class is a kind of net consumer that represent
 * a client for the ip connections.
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class NetClient<S extends NetSession, D extends Object> extends NetServiceConsumer<S, D> {

    private String host;

    public NetClient(String host, Integer port, NetService.TransportLayerProtocol protocol) {
        super(port, protocol);
        this.host = host;
    }

    /**
     * Put the client on the net service implementation
     */
    protected void connect() {
        NetService.getInstance().registerConsumer(this);
    }

    /**
     * Returns the host where it will connect the client
     * @return Remote host.
     */
    public String getHost() {
        return host;
    }

    /**
     * This method return the object that represent the
     * client's session.
     * @return Client's session.
     */
    public abstract S getSession();
}
