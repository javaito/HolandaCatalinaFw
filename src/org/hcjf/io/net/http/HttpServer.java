package org.hcjf.io.net.http;

import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

/**
 *
 */
public class HttpServer extends NetServer<HttpSession, HttpPackage>  {

    public HttpServer(Integer port, NetService.TransportLayerProtocol protocol, boolean multiSession) {
        super(port, protocol, multiSession);
    }

    /**
     * This method must implements the session creation based on
     * the net package that incoming.
     *
     * @param payLoad    Data to create the session.
     * @param netPackage Net package.
     * @return Return the session based on the package.
     */
    @Override
    protected HttpSession createSession(HttpPackage payLoad, NetPackage netPackage) {
        return null;
    }

    /**
     * This method encode the implementation data.
     *
     * @param payLoad Implementation data.
     * @return Implementation data encoded.
     */
    @Override
    protected final byte[] encode(HttpPackage payLoad) {
        return new byte[0];
    }

    /**
     * This method decode the net package to obtain the implementation data
     *
     * @param netPackage Net package.
     * @return Return the implementation data.
     */
    @Override
    protected final HttpPackage decode(NetPackage netPackage) {
        return null;
    }

    /**
     * Destroy the session.
     *
     * @param session Net session to be destroyed
     */
    @Override
    public void destroySession(NetSession session) {

    }

    @Override
    protected final void onRead(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        super.onRead(session, payLoad, netPackage);
    }

    /**
     * @param session
     * @param payLoad
     * @param netPackage
     */
    @Override
    protected final void onConnect(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        super.onConnect(session, payLoad, netPackage);
    }

    /**
     * @param session
     * @param payLoad
     * @param netPackage
     */
    @Override
    protected final void onDisconnect(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        super.onDisconnect(session, payLoad, netPackage);
    }

    /**
     * When the net service write data then call this method to process the package.
     *
     * @param session    Net session.
     * @param payLoad    Net package decoded.
     * @param netPackage Net package.
     */
    @Override
    protected final void onWrite(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        super.onWrite(session, payLoad, netPackage);
    }
}
