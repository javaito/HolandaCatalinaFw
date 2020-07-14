package org.hcjf.io.net.http.http2.frames;

public class PushPromiseFrame extends Http2Frame {

    protected PushPromiseFrame(Integer length, Type type) {
        super(length, Type.PUSH_PROMISE);
    }

    @Override
    protected void processPayload() {

    }
}
