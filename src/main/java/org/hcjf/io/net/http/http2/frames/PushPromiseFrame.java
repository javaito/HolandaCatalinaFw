package org.hcjf.io.net.http.http2.frames;

import java.nio.ByteBuffer;

public class PushPromiseFrame extends Http2Frame {

    public PushPromiseFrame(Integer id, Byte flags, Integer length) {
        super(id, flags, length, Type.PUSH_PROMISE);
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
