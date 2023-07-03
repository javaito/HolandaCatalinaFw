package org.hcjf.io.net.proxy;

import org.hcjf.io.net.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TcpProxy extends NetServer<TcpProxySession, ByteBuffer> {

    private List<TcpProxyClient> clients;

    public TcpProxy(Integer port) {
        super(port, NetService.TransportLayerProtocol.TCP, false, true);

        clients = new ArrayList<>();
    }

    @Override
    public TcpProxySession createSession(NetPackage netPackage) {
        TcpProxySession session = new TcpProxySession(UUID.randomUUID(), this, netPackage.getRemoteHost());

        clients.add(new TcpProxyClient(this, session, "localhost", 7555, true));
        clients.add(new TcpProxyClient(this, session, "localhost", 7556, false));

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
        for(TcpProxyClient client : clients) {
            ByteBuffer buffer = ByteBuffer.wrap(payLoad.array());
            client.onServerReceive(buffer);
        }
    }

    public void onClientResponse(TcpProxySession session, ByteBuffer payLoad, TcpProxyClient client) {
        if (client.getSession().getMain()) {
            try {
                write(session, payLoad);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
