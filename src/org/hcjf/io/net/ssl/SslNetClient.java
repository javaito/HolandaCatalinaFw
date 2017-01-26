package org.hcjf.io.net.ssl;


import org.hcjf.io.net.NetClient;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class SslNetClient extends NetClient {


    public SslNetClient(String host, Integer port, NetService.TransportLayerProtocol protocol) {
        super(host, port, protocol);
    }

    @Override
    public NetSession getSession() {
        return null;
    }

    @Override
    protected byte[] encode(Object payLoad) {
        return new byte[0];
    }

    @Override
    protected Object decode(NetPackage netPackage) {
        return null;
    }

    @Override
    public void destroySession(NetSession session) {

    }

    @Override
    public NetSession checkSession(NetSession session, Object payLoad, NetPackage netPackage) {
        return null;
    }
}
