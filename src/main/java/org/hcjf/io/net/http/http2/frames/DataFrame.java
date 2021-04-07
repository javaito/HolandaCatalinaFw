package org.hcjf.io.net.http.http2.frames;

import java.nio.ByteBuffer;

public class DataFrame extends Http2Frame {

    private Byte pathLength;
    private ByteBuffer padding;
    private ByteBuffer data;

    public DataFrame(Integer id, Byte flags, Integer length) {
        super(id, flags, length, Type.DATA);
    }

    public Byte getPathLength() {
        return pathLength;
    }

    public void setPathLength(Byte pathLength) {
        this.pathLength = pathLength;
    }

    public ByteBuffer getPadding() {
        return padding;
    }

    public void setPadding(ByteBuffer padding) {
        this.padding = padding;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
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
        fixedBuffer.put(getPathLength());
        fixedBuffer.put(getData());
        return fixedBuffer;
    }
}
