package org.hcjf.io.net.http.http2.frames;

import java.nio.ByteBuffer;

public class WindowsUpdateFrame extends Http2Frame {

    private Integer windowsSize;

    public WindowsUpdateFrame(Integer id, Byte flags, Integer length) {
        super(id, flags, length, Type.WINDOW_UPDATE);
    }

    @Override
    protected Integer recalculateLength() {
        return getLength();
    }

    @Override
    protected void processPayload() {
        windowsSize = getPayload().getInt();
    }

    @Override
    protected ByteBuffer serializePayload(ByteBuffer fixedBuffer) {
        fixedBuffer.putInt(windowsSize);
        return fixedBuffer;
    }
}
