package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.impl.messages.Message;
import org.hcjf.cloud.impl.messages.ShutdownMessage;
import org.hcjf.io.net.NetClient;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

import java.io.IOException;

/**
 * @author javaito
 */
public class CloudClient extends NetClient<CloudSession, MessageBuffer> {

    private final CloudSession session;
    private MessageBuffer messageBuffer;
    private Boolean connected;

    public CloudClient(String host, Integer port) {
        super(host, port, NetService.TransportLayerProtocol.TCP);
        this.session = new CloudSession(this);
        this.connected = null;
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

    public void send(Message message) throws IOException {
        MessageBuffer buffer = new MessageBuffer();
        buffer.append(message);
        write(session, buffer, true);
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
    }

    @Override
    protected MessageBuffer getShutdownPackage(CloudSession session) {
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.append(new ShutdownMessage(session));
        return messageBuffer;
    }

    public synchronized boolean waitForConnect() {
        if(connected == null) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        return connected;
    }

    @Override
    protected synchronized void onConnect(CloudSession session, MessageBuffer payLoad, NetPackage netPackage) {
        connected = true;
        notifyAll();
    }

    @Override
    protected synchronized void onConnectFail() {
        connected = false;
        notifyAll();
    }

    @Override
    protected void onRead(CloudSession session, MessageBuffer payLoad, NetPackage netPackage) {
        CloudOrchestrator.getInstance().incomingMessage(session, payLoad.getMessage());
    }

    @Override
    protected void onDisconnect(CloudSession session, NetPackage netPackage) {
        CloudOrchestrator.getInstance().connectionLost(session);
    }
}
