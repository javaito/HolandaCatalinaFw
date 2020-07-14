package org.hcjf.io.net.http.http2.frames;

public class PingFrame extends Http2Frame {

    protected PingFrame(Integer length, Type type) {
        super(length, Type.PING);
    }

    @Override
    protected void processPayload() {

    }
}
