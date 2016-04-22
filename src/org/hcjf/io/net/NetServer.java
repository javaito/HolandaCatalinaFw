package org.hcjf.io.net;

/**
 * This class is a kind of net consumer that represent
 * a server for the ip connections.
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class NetServer<S extends NetSession, D extends Object> extends NetServiceConsumer<S, D> {

    private final boolean multiSession;

    public NetServer(Integer port, NetService.TransportLayerProtocol protocol, boolean multiSession) {
        super(port, protocol);
        this.multiSession = multiSession;
    }

    /**
     * Public interface to create a session.
     * @param netPackage Package from the net
     * @return Net session created.
     */
    public final S createSession(NetPackage netPackage) {
        return createSession(decode(netPackage), netPackage);
    }

    /**
     * This method must implements the session creation based on
     * the net package that incoming.
     * @param payLoad Data to create the session.
     * @param netPackage Net package.
     * @return Return the session based on the package.
     */
    protected abstract S createSession(D payLoad, NetPackage netPackage);

    /**
     * This method return a flag to indicate if the
     * server is multisession or not
     * @return Return true if the server is multisession and false otherwise
     */
    public final boolean isMultiSession() {
        return multiSession;
    }
}
