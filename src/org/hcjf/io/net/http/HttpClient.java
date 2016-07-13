package org.hcjf.io.net.http;

import org.hcjf.io.net.NetClient;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class HttpClient extends NetClient<HttpSession, HttpPackage> {

    public HttpClient(String host, Integer port, NetService.TransportLayerProtocol protocol) {
        super(host, port, protocol);
    }

    /**
     * This method return the object that represent the
     * client's session.
     *
     * @return Client's session.
     */
    @Override
    public HttpSession getSession() {
        return null;
    }

    /**
     * This method decode the implementation data.
     *
     * @param payLoad Implementation data.
     * @return Implementation data encoded.
     */
    @Override
    protected byte[] encode(HttpPackage payLoad) {
        return new byte[0];
    }

    /**
     * This method decode the net package to obtain the implementation data
     *
     * @param netPackage Net package.
     * @return Return the implementation data.
     */
    @Override
    protected HttpPackage decode(NetPackage netPackage) {
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
}
