package org.hcjf.io.net.messages;

import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Cryptography;
import org.hcjf.utils.bson.BsonParcelable;

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
    private final Cryptography cryptography;

    public MessagesServer(Integer port, NetService.TransportLayerProtocol protocol,
                          boolean multiSession, boolean disconnectAndRemove) {
        this(port, protocol, multiSession, disconnectAndRemove, null);
    }

    public MessagesServer(Integer port, NetService.TransportLayerProtocol protocol,
                          boolean multiSession, boolean disconnectAndRemove, Cryptography cryptography) {
        super(port, protocol, multiSession, disconnectAndRemove);
        this.buffersBySession = new HashMap<>();
        this.cryptography = cryptography;
        if(SystemProperties.getBoolean(SystemProperties.Net.Messages.SERVER_DECOUPLED_IO_ACTION)) {
            decoupleIoAction(
                    SystemProperties.getInteger(SystemProperties.Net.Messages.SERVER_IO_QUEUE_SIZE),
                    SystemProperties.getInteger(SystemProperties.Net.Messages.SERVER_IO_WORKERS));
        }
    }

    /**
     * Returns true if the server use a encrypted protocol and false in the otherwise
     * @return Is encrypted or no.
     */
    public final boolean isEncrypted() {
        return cryptography != null;
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
     * @throws IOException Io Exception
     */
    public final void send(S session, Message message) throws IOException {
        MessageBuffer buffer = new MessageBuffer();
        buffer.append(isEncrypted() ? encrypt(message) : message);
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
                addDecoupledAction(new DecoupledAction(session) {
                    @Override
                    public void onAction() {
                        onRead(session, isEncrypted() ? decrypt((EncryptedMessage) message) : message);
                    }
                });
            }
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
     * This method depends of each implementation.
     * @param session Net session.
     * @param message Incoming message.
     */
    protected abstract void onRead(S session, Message message);
}
