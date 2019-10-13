package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.impl.messages.ShutdownMessage;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetSession;
import org.hcjf.io.net.messages.Message;
import org.hcjf.io.net.messages.MessageBuffer;
import org.hcjf.io.net.messages.MessagesNode;

/**
 * @author javaito
 */
public class CloudClient extends MessagesNode<CloudSession> {

    private final CloudSession session;

    public CloudClient(String host, Integer port) {
        super(host, port);
        this.session = new CloudSession(this);
    }

    @Override
    public CloudSession getSession() {
        return session;
    }

    @Override
    public CloudSession checkSession(CloudSession session, MessageBuffer payLoad, NetPackage netPackage) {
        return session;
    }

    public void disconnect() {
        disconnect(session, "");
    }

    @Override
    public void destroySession(NetSession session) {
    }

    @Override
    protected MessageBuffer getShutdownPackage(CloudSession session) {
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.append(new ShutdownMessage(session));
        return messageBuffer;
    }

    @Override
    protected void onRead(CloudSession session, Message message) {
        CloudOrchestrator.getInstance().incomingMessage(session, message);
    }

    @Override
    protected void onDisconnect(CloudSession session, NetPackage netPackage) {
    }
}
