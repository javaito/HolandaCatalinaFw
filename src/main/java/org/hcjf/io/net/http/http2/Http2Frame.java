package org.hcjf.io.net.http.http2;

/**
 * @author javaito.
 */
public class Http2Frame {

    private final Type type;
    private Byte flags;
    private Integer id;
    private byte[] payload;

    protected Http2Frame(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Byte getFlags() {
        return flags;
    }

    public void setFlags(Byte flags) {
        this.flags = flags;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    protected enum Type {

        DATA((byte)0x0),

        HEADERS((byte)0x1),

        PRIORITY((byte)0x2),

        RST_STREAM((byte)0x3),

        SETTINGS((byte)0x4),

        PUSH_PROMISE((byte)0x5),

        PING((byte)0x6),

        GO_AWAY((byte)0x7),

        WINDOW_UPDATE((byte)0x8),

        CONTINUATION((byte)0x9);

        private final byte id;

        Type(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
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
