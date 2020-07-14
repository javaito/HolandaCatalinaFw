package org.hcjf.io.net.http.http2.frames;

public class GoAwayFrame extends Http2Frame {

    protected GoAwayFrame(Integer length, Type type) {
        super(length, Type.GO_AWAY);
    }

    @Override
    protected void processPayload() {

    }
}
