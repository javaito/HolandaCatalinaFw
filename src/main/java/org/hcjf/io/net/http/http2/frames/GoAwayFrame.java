package org.hcjf.io.net.http.http2.frames;

import java.nio.ByteBuffer;

public class GoAwayFrame extends Http2Frame {

    private Integer lastStreamId;
    private Integer errorCode;
    private String additionalDebugData;

    public GoAwayFrame(Integer id, Byte flags, Integer length) {
        super(id, flags, length, Type.GO_AWAY);
    }

    public Integer getLastStreamId() {
        return lastStreamId;
    }

    public void setLastStreamId(Integer lastStreamId) {
        this.lastStreamId = lastStreamId;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getAdditionalDebugData() {
        return additionalDebugData;
    }

    public void setAdditionalDebugData(String additionalDebugData) {
        this.additionalDebugData = additionalDebugData;
    }

    @Override
    protected Integer recalculateLength() {
        return null;
    }

    @Override
    protected void processPayload() {
        setLastStreamId(getPayload().getInt());
        setErrorCode(getPayload().getInt());
        byte[] debugData = new byte[getLength() - 8];
        getPayload().get(debugData);
        setAdditionalDebugData(new String(debugData));
    }

    @Override
    protected ByteBuffer serializePayload(ByteBuffer fixedBuffer) {
        return null;
    }
}
