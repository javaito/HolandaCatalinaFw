package org.hcjf.io.net.messages;

import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.utils.bson.BsonParcelable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author javaito.
 */
public final class MessageBuffer {

    private List<Message> messages;
    private ByteBuffer buffer;
    private MessageBuffer leftover;

    public MessageBuffer() {
        this.messages = new ArrayList<>();
    }

    /**
     * Appends a message into the buffer.
     * @param message Message to append.
     */
    public synchronized void append(Message message) {
        this.messages.add(message);
        buffer = ByteBuffer.wrap(BsonEncoder.encode(message.toBson()));
    }

    /**
     * Appends data into the buffer.
     * @param data Data to append.
     */
    public synchronized void append(byte[] data) {
        if(messages.isEmpty()) {
            if (buffer == null) {
                buffer = ByteBuffer.wrap(data);
                buffer.rewind();
            } else {
                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + data.length);
                buffer.rewind();
                newBuffer.put(buffer);
                newBuffer.put(data);
                buffer = newBuffer;
                buffer.rewind();
            }

            if(buffer.capacity() > 4) {
                int bsonObjectSize = buffer.getInt();
                if (bsonObjectSize == buffer.capacity()) {
                    buffer.rewind();
                    messages.add(BsonParcelable.Builder.create(BsonDecoder.decode(buffer.array())));
                } else if (bsonObjectSize < buffer.capacity()) {
                    byte[] bsonObjectBody = new byte[bsonObjectSize];
                    byte[] leftOverBody = new byte[buffer.capacity() - bsonObjectSize];
                    buffer.rewind();
                    buffer.get(bsonObjectBody);
                    messages.add(BsonParcelable.Builder.create(BsonDecoder.decode(buffer.array())));
                    buffer.get(leftOverBody);
                    MessageBuffer leftover = new MessageBuffer();
                    leftover.append(leftOverBody);
                    if(leftover.isComplete()) {
                        messages.addAll(leftover.getMessages());
                        this.leftover = leftover.getLeftover();
                    } else {
                        this.leftover = leftover;
                    }
                }
            }
        } else {
            if(leftover == null) {
                leftover = new MessageBuffer();
            }
            leftover.append(data);
        }
    }

    /**
     * This method indicate when buffer contains some massage decoded.
     * @return Returns true if the buffer contains messages decoded or false in the otherwise.
     */
    public synchronized boolean isComplete() {
        return !messages.isEmpty();
    }

    /**
     * Returns the messages decoded contained into the buffer.
     * @return List of decoded messages.
     */
    public List<Message> getMessages() {
        List<Message> result = new ArrayList<>();
        result.addAll(messages);
        messages.clear();
        return result;
    }

    /**
     * Returns the left over data that is a incomplete message.
     * @return Left over data.
     */
    public MessageBuffer getLeftover() {
        return leftover;
    }

    /**
     * Returns the byte array that represent the message.
     * @return Internal byte array.
     */
    public synchronized byte[] getBytes() {
        buffer.rewind();
        return buffer.array();
    }
}
