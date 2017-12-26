package org.hcjf.io.net.http.http2;

import org.hcjf.properties.SystemProperties;

/**
 * @author javaito.
 */
public class StreamSettings {

    private Integer headerTableSize;
    private Boolean enablePush;
    private Integer maxConcurrentStream;
    private Integer initialWindowSize;
    private Integer maxFrameSize;
    private Integer maxHeaderListSize;

    public StreamSettings() {
        setHeaderTableSize(SystemProperties.getInteger(SystemProperties.Net.Http.Http2Settings.HEADER_TABLE_SIZE));
        setEnablePush(SystemProperties.getBoolean(SystemProperties.Net.Http.Http2Settings.ENABLE_PUSH));
        setMaxConcurrentStream(SystemProperties.getInteger(SystemProperties.Net.Http.Http2Settings.MAX_CONCURRENT_STREAMS));
        setInitialWindowSize(SystemProperties.getInteger(SystemProperties.Net.Http.Http2Settings.INITIAL_WINDOWS_SIZE));
        setMaxFrameSize(SystemProperties.getInteger(SystemProperties.Net.Http.Http2Settings.MAX_FRAME_SIZE));
        setMaxHeaderListSize(SystemProperties.getInteger(SystemProperties.Net.Http.Http2Settings.MAX_HEADER_LIST_SIZE));
    }

    public Integer getHeaderTableSize() {
        return headerTableSize;
    }

    public void setHeaderTableSize(Integer headerTableSize) {
        this.headerTableSize = headerTableSize;
    }

    public Boolean getEnablePush() {
        return enablePush;
    }

    public void setEnablePush(Boolean enablePush) {
        this.enablePush = enablePush;
    }

    public Integer getMaxConcurrentStream() {
        return maxConcurrentStream;
    }

    public void setMaxConcurrentStream(Integer maxConcurrentStream) {
        this.maxConcurrentStream = maxConcurrentStream;
    }

    public Integer getInitialWindowSize() {
        return initialWindowSize;
    }

    public void setInitialWindowSize(Integer initialWindowSize) {
        this.initialWindowSize = initialWindowSize;
    }

    public Integer getMaxFrameSize() {
        return maxFrameSize;
    }

    public void setMaxFrameSize(Integer maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    public Integer getMaxHeaderListSize() {
        return maxHeaderListSize;
    }

    public void setMaxHeaderListSize(Integer maxHeaderListSize) {
        this.maxHeaderListSize = maxHeaderListSize;
    }
}
