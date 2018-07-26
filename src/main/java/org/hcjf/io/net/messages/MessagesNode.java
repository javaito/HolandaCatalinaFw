package org.hcjf.io.net.messages;

import org.hcjf.io.net.NetClient;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

import java.io.IOException;

/**
 * This class provides the base to implements a message node.
 * @author javaito
 */
public abstract class MessagesNode<S extends NetSession> extends NetClient<S, MessageBuffer> {

    private MessageBuffer messageBuffer;
    private Boolean connected;

    public MessagesNode(String host, Integer port) {
        super(host, port, NetService.TransportLayerProtocol.TCP);
        this.connected = null;
    }

    /**
     * Encode buffer information to transfer the messages.
     * @param payLoad Implementation data.
     * @return Byte array.
     */
    @Override
    protected final byte[] encode(MessageBuffer payLoad) {
        return payLoad.getBytes();
    }

    /**
     * Decode the bytes into the net package and append this into the internal buffer.
     * @param netPackage Net package.
     * @return Returns the instance of message buffer.
     */
    @Override
    protected final synchronized MessageBuffer decode(NetPackage netPackage) {
        MessageBuffer message = this.messageBuffer;
        if(message == null) {
            message = new MessageBuffer();
        }
        message.append(netPackage.getPayload());
        return message;
    }

    /**
     * Send a message to the server.
     * @param message Message instance.
     * @throws IOException
     */
    public final void send(Message message) throws IOException {
        MessageBuffer buffer = new MessageBuffer();
        buffer.append(message);
        write(getSession(), buffer, false);
    }

    /**
     * Wait until the node is connected with the server.
     * @return Connection status, if the result is true then the node connection was successful but if the
     * response is false the something is not work.
     */
    public final synchronized boolean waitForConnect() {
        if(connected == null) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        return connected;
    }

    /**
     * This method is called when the node is connected.
     * @param session Connected session.
     * @param payLoad Decoded package payload.
     * @param netPackage Original package.
     */
    @Override
    protected final synchronized void onConnect(S session, MessageBuffer payLoad, NetPackage netPackage) {
        connected = true;
        notifyAll();
    }

    /**
     * This method is called when the node connection fail.
     */
    @Override
    protected final synchronized void onConnectFail() {
        connected = false;
        notifyAll();
    }

    /**
     * This method return the connected status of the node.
     * @return Returns true if the node is connected and false in the otherwise
     */
    public final boolean isConnected() {
        return connected;
    }

    @Override
    protected void onDisconnect(S session, NetPackage netPackage) {
        connected = false;
    }

    /**
     * This method is called when the node read information from the net.
     * @param session Net session.
     * @param payLoad Net package decoded
     * @param netPackage Net package.
     */
    @Override
    protected final void onRead(S session, MessageBuffer payLoad, NetPackage netPackage) {
        if(payLoad.isComplete()) {
            for(Message message : payLoad.getMessages()) {
                onRead(session, message);
            }
            this.messageBuffer = payLoad.getLeftover();
        }
    }

    /**
     * This method is called when there are a complete message into the buffer.
     * @param session Net session.
     * @param incomingMessage Incoming message.
     */
    protected abstract void onRead(S session, Message incomingMessage);
}
