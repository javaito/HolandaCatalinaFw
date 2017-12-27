package org.hcjf.cloud.impl;

import org.hcjf.io.net.NetClient;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

/**
 * @author javaito
 */
public class CloudClient extends NetClient<Nodes.Node, MessageBuffer> {

    private final Nodes.Node node;
    private MessageBuffer messageBuffer;

    public CloudClient(Nodes.Node node) {
        super(node.getRemoteHost(), node.getRemotePort(), NetService.TransportLayerProtocol.TCP);
        this.node = node;
    }

    @Override
    public Nodes.Node getSession() {
        return node;
    }

    @Override
    public Nodes.Node checkSession(Nodes.Node session, MessageBuffer payLoad, NetPackage netPackage) {
        return session;
    }

    @Override
    protected byte[] encode(MessageBuffer payLoad) {
        return payLoad.getBytes();
    }

    @Override
    protected MessageBuffer decode(NetPackage netPackage) {
        MessageBuffer message = this.messageBuffer;
        if(message == null) {
            message = new MessageBuffer();
        }
        message.append(netPackage.getPayload());
        if(message.isComplete()) {
            messageBuffer = null;
        }
        return message;
    }

    @Override
    public void destroySession(NetSession session) {
        if(session instanceof Nodes.Node) {
            Nodes.disconnected((Nodes.Node)session);
        }
    }

}
