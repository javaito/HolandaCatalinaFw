package org.hcjf.io.net.http.http2.frames;

import java.nio.ByteBuffer;

public class HeadersFrame extends Http2Frame {

    public HeadersFrame(Integer id, Byte flags, Integer length) {
        super(id, flags, length, Type.HEADERS);
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
