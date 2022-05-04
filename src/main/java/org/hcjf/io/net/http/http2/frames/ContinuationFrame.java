package org.hcjf.io.net.http.http2.frames;

import java.nio.ByteBuffer;

public class ContinuationFrame extends Http2Frame {

    public ContinuationFrame(Integer id, Byte flags, Integer length) {
        super(id, flags, length, Type.CONTINUATION);
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
        return fixedBuffer;
    }
}
