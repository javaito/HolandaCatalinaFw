package org.hcjf.io.net.http.http2;

/**
 * @author javaito.
 */
public class Stream {

    private final Integer headerTableSize;
    private final Boolean enablePush;
    private final Integer maxConcurrentStream;
    private final Integer initialWindowSize;
    private final Integer maxFrameSize;
    private final Integer maxHeaderListSize;

    public Stream(StreamSettings settings) {
        headerTableSize = settings.getHeaderTableSize();
        enablePush = settings.getEnablePush();
        maxConcurrentStream = settings.getMaxConcurrentStream();
        initialWindowSize = settings.getInitialWindowSize();
        maxFrameSize = settings.getMaxFrameSize();
        maxHeaderListSize = settings.getMaxHeaderListSize();
    }

    public Integer getHeaderTableSize() {
        return headerTableSize;
    }

    public Boolean getEnablePush() {
        return enablePush;
    }

    public Integer getMaxConcurrentStream() {
        return maxConcurrentStream;
    }

    public Integer getInitialWindowSize() {
        return initialWindowSize;
    }

    public Integer getMaxFrameSize() {
        return maxFrameSize;
    }

    public Integer getMaxHeaderListSize() {
        return maxHeaderListSize;
    }
}
