package org.hcjf.io.net.http.http2.frames;

public class WindowsUpdateFrame extends Http2Frame {

    protected WindowsUpdateFrame(Integer length, Type type) {
        super(length, Type.WINDOW_UPDATE);
    }

    @Override
    protected void processPayload() {

    }
}
