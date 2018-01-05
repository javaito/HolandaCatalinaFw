package org.hcjf.cloud.impl.network;

import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.cloud.impl.messages.Message;
import org.hcjf.utils.bson.BsonParcelable;

import java.nio.ByteBuffer;

/**
 * @author javaito.
 */
public final class MessageBuffer {

    private Message message;
    private ByteBuffer buffer;

    public synchronized void append(Message message) {
        this.message = message;
        buffer = ByteBuffer.wrap(BsonEncoder.encode(message.toBson()));
    }

    public synchronized void append(byte[] data) {
        if(message == null) {
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
                if (buffer.getInt() == buffer.capacity()) {
                    buffer.rewind();
                    message = BsonParcelable.Builder.create(BsonDecoder.decode(buffer.array()));
                }
            }
        }
    }

    public synchronized boolean isComplete() {
        return message != null;
    }

    public Message getMessage() {
        return message;
    }

    public synchronized byte[] getBytes() {
        buffer.rewind();
        return buffer.array();
    }
}
