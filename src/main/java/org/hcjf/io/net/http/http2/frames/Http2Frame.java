package org.hcjf.io.net.http.http2.frames;

import org.hcjf.errors.HCJFRuntimeException;

import java.nio.ByteBuffer;

/**
 * @author javaito.
 */
public abstract class Http2Frame {

    private final Integer length;
    private final Type type;
    private Byte flags;
    private Integer id;
    private ByteBuffer payload;
    private Boolean complete;

    protected Http2Frame(Integer length, Type type) {
        this.length = length;
        this.type = type;
        this.complete = false;
        this.payload = ByteBuffer.allocate(length);
    }

    public final Type getType() {
        return type;
    }

    public final Byte getFlags() {
        return flags;
    }

    public final void setFlags(Byte flags) {
        this.flags = flags;
    }

    public final Integer getId() {
        return id;
    }

    public final void setId(Integer id) {
        this.id = id;
    }

    public final ByteBuffer getPayload() {
        return payload;
    }

    public final void setPayload(ByteBuffer payload) {
        this.payload = payload;
    }

    public final Boolean getComplete() {
        return complete;
    }

    protected final void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public final void addData(ByteBuffer data) {
        if(!getComplete()) {
            payload.put(data);
            if (payload.position() == payload.limit()) {
                setComplete(true);
                payload.rewind();
                processPayload();
            }
        }
    }

    protected abstract void processPayload();

    public static final class Builder {

        public static <F extends Http2Frame> F build(Integer length, Byte type) {
            Class<? extends Http2Frame> frameClass = Type.getClassById(type);
            if(frameClass == null) {
                throw new HCJFRuntimeException("Frame type not found: %d", type);
            }
            try {
                return (F) frameClass.getConstructor(Integer.class, Byte.class).newInstance(length, type);
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

    public enum Settings {

        SETTINGS_HEADER_TABLE_SIZE((byte)0x1),

        SETTINGS_ENABLE_PUSH((byte)0x2),

        SETTINGS_MAX_CONCURRENT_STREAMS((byte)0x3),

        SETTINGS_INITIAL_WINDOW_SIZE((byte)0x4),

        SETTINGS_MAX_FRAME_SIZE((byte)0x5),

        SETTINGS_MAX_HEADER_LIST_SIZE((byte)0x6);

        private final byte id;

        Settings(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
        }

    }
}
