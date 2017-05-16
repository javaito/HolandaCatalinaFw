package org.hcjf.io.net;

/**
 * This class is a kind of net consumer that represent
 * a server for the ip connections.
 * @author javaito
 */
public abstract class NetServer<S extends NetSession, D extends Object> extends NetServiceConsumer<S, D> {

    private final boolean multiSession;
    private final boolean disconnectAndRemove;

    public NetServer(Integer port, NetService.TransportLayerProtocol protocol,
                     boolean multiSession, boolean disconnectAndRemove) {
        super(port, protocol);
        this.multiSession = multiSession;
        this.disconnectAndRemove = disconnectAndRemove;
    }

    /**
     * This method must implements the session creation based on
     * the net package that incoming.
     * @param netPackage Net package.
     * @return Return the session based on the package.
     */
    public abstract S createSession(NetPackage netPackage);

    /**
     * This method return a flag to indicate if the
     * server is multisession or not
     * @return Return true if the server is multisession and false otherwise
     */
    public final boolean isMultiSession() {
        return multiSession;
    }

    /**
     * This method indicate if the server must destroy de session when
     * the session is disconnected.
     * @return True to destroy and false otherwise
     */
    public final boolean isDisconnectAndRemove() {
        return disconnectAndRemove;
    }

    /**
     * This method starts the server.
     */
    public final void start() {
        NetService.getInstance().registerConsumer(this);
        onStart();
    }

    /**
     * This method should be overwritten to know when the server is started
     */
    protected void onStart(){}

    /**
     * This method stops the server.
     */
    public final void stop() {
        //TODO: Need to method to unregister the consumer.
        onStop();
    }

    /**
     * This method should be overwritten to know when the server is stopped
     */
    protected void onStop(){}

}
