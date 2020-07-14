package org.hcjf.io.net.http.http2.frames;

public class SettingsFrame extends Http2Frame {

    protected SettingsFrame(Integer length, Type type) {
        super(length, Type.SETTINGS);
    }

    @Override
    protected void processPayload() {

    }
}
