package org.hcjf.io.net.messages;

import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This implementation provides the base to work with messages into the network.
 * The messages are objects that are serialized in bson format to be transfer.
 * @author javaito
 */
public abstract class MessagesServer<S extends NetSession> extends NetServer<S, MessageBuffer> {

    private final Map<S,MessageBuffer> buffersBySession;

    public MessagesServer(Integer port, NetService.TransportLayerProtocol protocol, boolean multiSession, boolean disconnectAndRemove) {
        super(port, protocol, multiSession, disconnectAndRemove);
        buffersBySession = new HashMap<>();
    }

    /**
     * Returns the byte array in order to transfer the message.
     * @param payLoad Implementation data.
     * @return Byte array that represents the message.
     */
    @Override
    protected final byte[] encode(MessageBuffer payLoad) {
        return payLoad.getBytes();
    }

    /**
     * Decode the information from the network and append this into the internal message buffer.
     * @param netPackage Net package.
     * @return Returns the message buffer with all the information.
     */
    @Override
    protected final synchronized MessageBuffer decode(NetPackage netPackage) {
        MessageBuffer messageBuffer = buffersBySession.remove(netPackage.getSession());
        if(messageBuffer == null) {
            messageBuffer = new MessageBuffer();
        }
        messageBuffer.append(netPackage.getPayload());

        if(messageBuffer.isComplete()) {
            buffersBySession.put((S) netPackage.getSession(), messageBuffer.getLeftover());
        } else {
            buffersBySession.put((S) netPackage.getSession(), messageBuffer);
        }

        return messageBuffer;
    }

    /**
     * Send message to the net using the buffer.
     * @param session Session that indicate the remote host.
     * @param message Message to transfer.
     * @throws IOException
     */
    public final void send(S session, Message message) throws IOException {
        MessageBuffer buffer = new MessageBuffer();
        buffer.append(message);
        write(session, buffer, false);
    }

    /**
     * This method destroy the buffer for the specific session.
     * @param session Net session to be destroyed
     */
    @Override
    public void destroySession(NetSession session) {
        buffersBySession.remove(session);
    }

    /**
     * This method is called when there are data for some session.
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
        }
    }

    /**
     * This method depends of each implementation.
     * @param session Net session.
     * @param message Incoming message.
     */
    protected abstract void onRead(S session, Message message);
}
