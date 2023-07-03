package org.hcjf.io.net.proxy;

import org.hcjf.io.net.NetClient;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class TcpProxyClient extends NetClient<TcpProxySession, ByteBuffer> {

    private final TcpProxy proxy;
    private final TcpProxySession serverSession;
    private final Boolean main;
    private final TcpProxySession session;

    public TcpProxyClient(TcpProxy proxy, TcpProxySession serverSession, String host, Integer port, Boolean main) {
        super(host, port, NetService.TransportLayerProtocol.TCP);
        this.main = main;
        this.proxy = proxy;
        this.serverSession = serverSession;
        this.session = new TcpProxySession(UUID.randomUUID(), this, getHost(), true, main);
        connect();
    }

    @Override
    public TcpProxySession getSession() {
        return session;
    }

    @Override
    protected byte[] encode(ByteBuffer payLoad) {
        byte[] newBuffer = new byte[payLoad.remaining()];
        payLoad.get(newBuffer);
        return newBuffer;
    }

    @Override
    protected ByteBuffer decode(NetPackage netPackage) {
        return ByteBuffer.wrap(netPackage.getPayload());
    }

    @Override
    public void destroySession(NetSession session) {

    }

    @Override
    public TcpProxySession checkSession(TcpProxySession session, ByteBuffer payLoad, NetPackage netPackage) {
        return session;
    }

    @Override
    protected void onRead(TcpProxySession session, ByteBuffer payLoad, NetPackage netPackage) {
        proxy.onClientResponse(serverSession, payLoad, this);
    }

    public void onServerReceive(ByteBuffer payLoad) {
        try {
            write(session, payLoad);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
