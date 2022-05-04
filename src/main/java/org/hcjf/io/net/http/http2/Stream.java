package org.hcjf.io.net.http.http2;

import org.hcjf.io.net.http.http2.frames.Http2Frame;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Strings;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author javaito.
 */
public class Stream {

    private final Integer streamId;
    private final Integer headerTableSize;
    private final Boolean enablePush;
    private final Integer maxConcurrentStream;
    private final Integer initialWindowSize;
    private final Integer maxFrameSize;
    private final Integer maxHeaderListSize;
    private String http2Preface;
    private final Queue<Http2Frame> frames;
    private final ByteBuffer buffer;

    public Stream(Integer streamId, StreamSettings settings) {
        this.streamId = streamId;
        headerTableSize = settings.getHeaderTableSize();
        enablePush = settings.getEnablePush();
        maxConcurrentStream = settings.getMaxConcurrentStream();
        initialWindowSize = settings.getInitialWindowSize();
        maxFrameSize = settings.getMaxFrameSize();
        maxHeaderListSize = settings.getMaxHeaderListSize();
        frames = new ArrayBlockingQueue<>(SystemProperties.getInteger(SystemProperties.Net.Http.Http2.STREAM_FRAMES_QUEUE_MAX_SIZE));
        buffer = ByteBuffer.allocate(maxFrameSize + Http2Frame.FRAME_HEADER_LENGTH);
    }

    public Integer getStreamId() {
        return streamId;
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

    public String getHttpClientPreface() {
        return http2Preface;
    }

    public Collection<Http2Frame> getAndRemoveFrames() {
        Collection<Http2Frame> result = new ArrayList<>();
        while(!frames.isEmpty()) {
            result.add(frames.remove());
        }
        return result;
    }

    public void setHttpClientPreface(String http2Preface) {
        this.http2Preface = http2Preface;
    }

    public synchronized final void addData(byte[] data, int start) {
        System.out.println("Add data>>>!!!: " + Strings.bytesToHex(data));
        Integer dataStart = start;
        Integer dataLength = (data.length - start) - (buffer.capacity() - buffer.position());
        if(dataLength < 0) {
            dataLength = data.length - start;
        }
        buffer.put(data, dataStart, dataLength);
        buffer.flip();
        while(true) {
            if (buffer.limit() >= Http2Frame.FRAME_HEADER_LENGTH) {
                Integer length = ((buffer.get() & 0x0F) << 16) | ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
                Byte type = buffer.get();
                Byte flags = buffer.get();
                Integer id = (((buffer.get() & ~0b10000000) & 0x0F) << 24) | ((buffer.get() & 0xFF) << 16) | ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
                if (buffer.limit() >= length) {
                    Http2Frame frame = Http2Frame.Builder.build(id, flags, length, type);
                    byte[] completeFrameData = new byte[length];
                    buffer.get(completeFrameData);
                    frame.setPayload(ByteBuffer.wrap(completeFrameData));
                    frames.add(frame);
                    if(buffer.limit() - buffer.position() > 0) {
                        byte[] rest = new byte[buffer.limit() - (length + Http2Frame.FRAME_HEADER_LENGTH)];
                        buffer.get(rest);
                        buffer.clear();
                        buffer.put(rest);
                        buffer.flip();
                    } else {
                        buffer.clear();
                        break;
                    }
                } else {
                    buffer.position(0);
                    break;
                }
            }
        }
    }

}
