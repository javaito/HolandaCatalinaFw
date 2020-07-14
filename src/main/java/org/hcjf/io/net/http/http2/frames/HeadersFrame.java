package org.hcjf.io.net.http.http2.frames;

public class HeadersFrame extends Http2Frame {

    protected HeadersFrame(Integer length, Type type) {
        super(length, Type.HEADERS);
    }

    @Override
    protected void processPayload() {

    }
}
