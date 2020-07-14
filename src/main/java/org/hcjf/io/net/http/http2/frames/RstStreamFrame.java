package org.hcjf.io.net.http.http2.frames;

public class RstStreamFrame extends Http2Frame {

    protected RstStreamFrame(Integer length, Type type) {
        super(length, Type.RST_STREAM);
    }

    @Override
    protected void processPayload() {

    }
}
