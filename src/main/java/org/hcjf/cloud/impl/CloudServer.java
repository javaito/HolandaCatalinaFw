package org.hcjf.cloud.impl;

import org.hcjf.cloud.impl.messages.Message;
import org.hcjf.cloud.impl.messages.NodeIdentificationMessage;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 */
public class CloudServer extends NetServer<Nodes.Node, MessageBuffer> {

    private final Map<Nodes.Node,MessageBuffer> buffersBySession;

    public CloudServer() {
        super(18080, NetService.TransportLayerProtocol.TCP,
                false, true);
        buffersBySession = new HashMap<>();
    }

    @Override
    public Nodes.Node createSession(NetPackage netPackage) {
        Nodes.Node node = Nodes.createNode(netPackage.getRemoteHost(), this);
        if(!Nodes.connecting(node)) {
            node = null;
        }
        return node;
    }

    @Override
    public Nodes.Node checkSession(Nodes.Node session, MessageBuffer payLoad, NetPackage netPackage) {
        //TODO: Do something to check if the connection is invalid
        return session;
    }

    @Override
    protected byte[] encode(MessageBuffer payLoad) {
        return payLoad.getBytes();
    }

    @Override
    protected MessageBuffer decode(NetPackage netPackage) {
        MessageBuffer messageBuffer = buffersBySession.remove(netPackage.getSession());
        if(messageBuffer == null) {
            messageBuffer = new MessageBuffer();
        }
        messageBuffer.append(netPackage.getPayload());
        if(!messageBuffer.isComplete()) {
            buffersBySession.put((Nodes.Node) netPackage.getSession(), messageBuffer);
        }
        return messageBuffer;
    }

    @Override
    public void destroySession(NetSession session) {
        if(session instanceof Nodes.Node) {
            buffersBySession.remove(session);
            Nodes.disconnected((Nodes.Node)session);
        }
    }

    @Override
    protected void onRead(Nodes.Node session, MessageBuffer payLoad, NetPackage netPackage) {
        if(payLoad.isComplete()) {
            Message message = payLoad.getMessage();
            if (session.getStatus().equals(Nodes.Node.Status.CONNECTING)) {
                if (message instanceof NodeIdentificationMessage) {
                    Nodes.updateNode(session, (NodeIdentificationMessage) message);
                } else {
                    disconnect(session, "Unidentifiable session: " + session.getHostPort());
                }
            } else if(session.getStatus().equals(Nodes.Node.Status.CONNECTED)) {

            } else if(session.getStatus().equals(Nodes.Node.Status.DISCONNECTED)) {
                disconnect(session, "Unidentifiable session: " + session.getHostPort());
            }
        }
    }
}
