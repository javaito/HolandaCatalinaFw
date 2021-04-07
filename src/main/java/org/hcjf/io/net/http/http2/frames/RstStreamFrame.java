package org.hcjf.io.net.http.http2.frames;

import java.nio.ByteBuffer;

public class RstStreamFrame extends Http2Frame {

    public RstStreamFrame(Integer id, Byte flags, Integer length) {
        super(id, flags, length, Type.RST_STREAM);
    }

    @Override
    protected Integer recalculateLength() {
        return null;
    }

    @Override
    protected void processPayload() {

    }

    @Override
    protected ByteBuffer serializePayload(ByteBuffer fixedBuffer) {
        return null;
    }
}
