package org.hcjf.io.net.http.http2.frames;

public class PriorityFrame extends Http2Frame {

    protected PriorityFrame(Integer length, Type type) {
        super(length, Type.PRIORITY);
    }

    @Override
    protected void processPayload() {

    }
}
