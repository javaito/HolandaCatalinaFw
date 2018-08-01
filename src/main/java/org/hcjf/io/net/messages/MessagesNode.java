package org.hcjf.io.net.messages;

import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.io.net.NetClient;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Cryptography;
import org.hcjf.utils.bson.BsonParcelable;

import java.io.IOException;

/**
 * This class provides the base to implements a message node.
 * @author javaito
 */
public abstract class MessagesNode<S extends NetSession> extends NetClient<S, MessageBuffer> {

    private MessageBuffer messageBuffer;
    private Boolean connected;
    private final Cryptography cryptography;

    public MessagesNode(String host, Integer port) {
        this(host, port, null);
    }

    public MessagesNode(String host, Integer port, Cryptography cryptography) {
        super(host, port, NetService.TransportLayerProtocol.TCP);
        this.connected = null;
        this.cryptography = cryptography;
    }

    /**
     * Returns true if the server use a encrypted protocol and false in the otherwise
     * @return Is encrypted or no.
     */
    public boolean isEncrypted() {
        return cryptography != null;
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
        if(messageBuffer == null) {
            messageBuffer = new MessageBuffer();
        }
        messageBuffer.append(netPackage.getPayload());
        return messageBuffer;
    }

    /**
     * Send a message to the server.
     * @param message Message instance.
     * @throws IOException
     */
    public final void send(Message message) throws IOException {
        MessageBuffer buffer = new MessageBuffer();
        buffer.append(isEncrypted() ? encrypt(message) : message);
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
    protected final synchronized void onRead(S session, MessageBuffer payLoad, NetPackage netPackage) {
        if(payLoad.isComplete()) {
            for(Message message : payLoad.getMessages()) {
                try {
                    onRead(session, isEncrypted() ? decrypt((EncryptedMessage) message) : message);
                } catch (ClassCastException ex) {
                    Log.w(SystemProperties.get(SystemProperties.Net.Messages.LOG_TAG),
                            "Incoming not encrypted message and the server has a cryptography policy");
                }
            }
            this.messageBuffer = payLoad.getLeftover();
        }
    }

    /**
     * This method must encrypt the message and create an instance of {@link EncryptedMessage}
     * wrapping the original message.
     * @param message Original message.
     * @return Encrypted message.
     */
    protected EncryptedMessage encrypt(Message message) {
        EncryptedMessage encryptedMessage = new EncryptedMessage();
        encryptedMessage.setId(message.getId());
        encryptedMessage.setSessionId(message.getSessionId());
        encryptedMessage.setTimestamp(message.getTimestamp());
        encryptedMessage.setEncrypedData(cryptography.encrypt(
                BsonEncoder.encode(message.toBson())));
        return encryptedMessage;
    }

    /**
     * This method must decrypt the encrypted message and returns the original message decrypted.
     * @param encryptedMessage Incoming encrypted message.
     * @return Original message decrypted.
     */
    protected Message decrypt(EncryptedMessage encryptedMessage) {
        BsonDocument document = BsonDecoder.decode(cryptography.decrypt(
                encryptedMessage.getEncrypedData()));
        return BsonParcelable.Builder.create(document);
    }

    /**
     * This method is called when there are a complete message into the buffer.
     * @param session Net session.
     * @param incomingMessage Incoming message.
     */
    protected abstract void onRead(S session, Message incomingMessage);
}
