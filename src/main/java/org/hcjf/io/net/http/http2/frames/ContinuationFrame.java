package org.hcjf.io.net.http.http2.frames;

public class ContinuationFrame extends Http2Frame {

    protected ContinuationFrame(Integer length) {
        super(length, Type.CONTINUATION);
    }

    @Override
    protected void processPayload() {

    }
}
