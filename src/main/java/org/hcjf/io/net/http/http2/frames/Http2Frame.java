package org.hcjf.io.net.http.http2.frames;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.Strings;

import java.nio.ByteBuffer;

/**
 * @author javaito.
 */
public abstract class Http2Frame {

    //All the frames start with 9 octets as headers
    //https://httpwg.org/specs/rfc7540.html#FramingLayer
    public static final Integer FRAME_HEADER_LENGTH = 9;

    private final Integer id;
    private final Byte flags;
    private Integer length;
    private final Type type;
    private ByteBuffer payload;

    protected Http2Frame(Integer id, Byte flags, Integer length, Type type) {
        this.id = id;
        this.flags = flags;
        this.length = length;
        this.type = type;
        this.payload = ByteBuffer.allocate(length);
    }

    public final Type getType() {
        return type;
    }

    public final Byte getFlags() {
        return flags;
    }

    public final Integer getLength() {
        return length;
    }

    public final Integer getId() {
        return id;
    }

    public final ByteBuffer getPayload() {
        return payload;
    }

    public final void setPayload(ByteBuffer payload) {
        this.payload = payload;
        processPayload();
    }

    public final ByteBuffer serialize() {
        length = recalculateLength();
        ByteBuffer fixedBuffer = ByteBuffer.allocate(getLength() + FRAME_HEADER_LENGTH);

        //Add length value into the fixed buffer
        byte[] lengthBytes = ByteBuffer.allocate(4).putInt(getLength()).array();
        for (int i = 1; i < 4; i++) {
            fixedBuffer.put(lengthBytes[i]);
        }

        //Add type value into fixed buffer
        fixedBuffer.put(getType().id);

        //Add flags value into fixed buffer
        fixedBuffer.put(getFlags());

        //Add id into fixed buffer
        fixedBuffer.putInt(getId());

        return serializePayload(fixedBuffer);
    }

    protected abstract Integer recalculateLength();

    protected abstract void processPayload();

    protected abstract ByteBuffer serializePayload(ByteBuffer fixedBuffer);

    public static final class Builder {

        public static <F extends Http2Frame> F build(Integer id, Byte flags, Integer length, Byte type) {
            Class<? extends Http2Frame> frameClass = Type.getClassById(type);
            if(frameClass == null) {
                throw new HCJFRuntimeException("Frame type not found: %d", type);
            }
            try {
                return (F) frameClass.getConstructor(Integer.class, Byte.class, Integer.class).newInstance(id, flags, length);
            } catch (Exception e) {
                throw new HCJFRuntimeException("Unable to create frame instance");
            }
        }

    }

    protected enum Type {

        DATA(DataFrame.class, (byte)0x0),

        HEADERS(HeadersFrame.class, (byte)0x1),

        PRIORITY(PriorityFrame.class, (byte)0x2),

        RST_STREAM(RstStreamFrame.class, (byte)0x3),

        SETTINGS(SettingsFrame.class, (byte)0x4),

        PUSH_PROMISE(PushPromiseFrame.class, (byte)0x5),

        PING(PingFrame.class, (byte)0x6),

        GO_AWAY(GoAwayFrame.class, (byte)0x7),

        WINDOW_UPDATE(WindowsUpdateFrame.class, (byte)0x8),

        CONTINUATION(ContinuationFrame.class, (byte)0x9);

        private final Class<? extends Http2Frame> frameClass;
        private final byte id;

        Type(Class<? extends Http2Frame> frameClass, byte id) {
            this.frameClass = frameClass;
            this.id = id;
        }

        public byte getId() {
            return id;
        }

        public static Class<? extends Http2Frame> getClassById(byte id) {
            Class<? extends Http2Frame> result = null;
            for(Type type : values()) {
                if(type.id == id) {
                    result = type.frameClass;
                }
            }
            return result;
        }
    }

    public enum Error {

        NO_ERROR((byte)0x0),

        PROTOCOL_ERROR((byte)0x1),

        INTERNAL_ERROR((byte)0x2),

        FLOW_CONTROL_ERROR((byte)0x3),

        SETTINGS_TIMEOUT((byte)0x4),

        STREAM_CLOSED((byte)0x5),

        FRAME_SIZE_ERROR((byte)0x6),

        REFUSED_STREAM((byte)0x7),

        CANCEL((byte)0x8),

        COMPRESSION_ERROR((byte)0x9),

        CONNECT_ERROR((byte)0xA),

        ENHANCE_YOUR_CALM((byte)0xB),

        INADEQUATE_SECURITY((byte)0xC),

        HTTP_1_1_REQUIRED((byte)0xD);

        private final byte id;

        Error(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
        }
    }

}
