package org.hcjf.io.net.http.http2.frames;

import java.nio.ByteBuffer;

public class PriorityFrame extends Http2Frame {

    public PriorityFrame(Integer id, Byte flags, Integer length) {
        super(id, flags, length, Type.PRIORITY);
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
