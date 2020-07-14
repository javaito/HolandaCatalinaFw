package org.hcjf.io.net.http.http2.frames;

public class DataFrame extends Http2Frame {

    protected DataFrame(Integer length, Type type) {
        super(length, Type.DATA);
    }

    @Override
    protected void processPayload() {

    }
}
