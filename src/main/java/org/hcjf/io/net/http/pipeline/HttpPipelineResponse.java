package org.hcjf.io.net.http.pipeline;

import org.hcjf.io.net.http.HttpResponse;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a way to create a pipeline between the http connection and
 * the specific data source.
 * @author javaito.
 */
public abstract class HttpPipelineResponse extends HttpResponse {

    private final ByteBuffer mainBuffer;
    private final StreamingPackage streamingPackage;
    private int readCounter;

    public HttpPipelineResponse(int mainBufferSize, int bufferSize) {
        this.mainBuffer = ByteBuffer.allocate(mainBufferSize);
        this.streamingPackage = new StreamingPackage(bufferSize);
        this.readCounter = 0;
    }

    /**
     * Verify if the first read over the pipeline.
     * @return Returns true if the first read operation and false in the otherwise.
     */
    public final boolean isFirstRead() {
        return getReadCounter() == 1;
    }

    /**
     * Returns the counter of the read operations over pipeline.
     * @return Counter of the read operations.
     */
    public final int getReadCounter() {
        return readCounter;
    }

    /**
     * This method reads the bytes of the application
     * side of the pipeline and wrap this bytes depends of the
     * result encoding.
     * @return Buffer with all the read bytes if the buffer size is equals
     * to -1 then this pipeline is done.
     */
    public final int read() {
        mainBuffer.rewind();
        streamingPackage.clear();
        int size = readPipeline(streamingPackage);
        readCounter++;
        size = wrap(mainBuffer, streamingPackage, size);
        return size;
    }

    /**
     * Return the buffer, this method must be call before the read method.
     * @return Main buffer with all the read bytes.
     */
    public final ByteBuffer getMainBuffer() {
        return mainBuffer;
    }

    /**
     * This method wrap the byte with the encoding protocol.
     * @param result In this instance of the byte buffer, this method must put
     * the byte to wrap the source data and the source data.
     * @param streamingPackage All the bytes read from the application source.
     * @param size Size of the buffer used to read the application source.
     * @return Byte array wrapped.
     */
    protected int wrap(ByteBuffer result, StreamingPackage streamingPackage, int size) {
        result.put(streamingPackage.getBuffer(), 0, size);
        return result.position();
    }

    /**
     * This method is called before the fir read over the pipeline.
     */
    public void onStart() {}

    /**
     * This method is called after the last read over the pipeline.
     */
    public void onEnd() {}

    /**
     * This method must implements the way to read the information from the
     * application source.
     * @param streamingPackage Buffer to put all the read bytes.
     * @return Number of bytes read.
     */
    protected abstract int readPipeline(StreamingPackage streamingPackage);

    /**
     * This class encapsulate the byte and custom properties send.
     */
    protected final class StreamingPackage {

        private final byte[] buffer;
        private final Map<String,Object> properties;

        public StreamingPackage(int bufferSize) {
            buffer = new byte[bufferSize];
            properties = new HashMap<>();
        }

        /**
         * Returns the buffer of the package.
         * @return Buffer of the package.
         */
        public byte[] getBuffer() {
            return buffer;
        }

        /**
         * Put a property into the package.
         * @param propertyName Property name.
         * @param propertyValue Property value.
         */
        public void put(String propertyName, Object propertyValue) {
            properties.put(propertyName, propertyValue);
        }

        /**
         * Returns the property value for the property name specified.
         * @param propertyName Property name.
         * @param <O> Expected property value type.
         * @return Property value.
         */
        public <O extends Object> O get(String propertyName) {
            return (O) properties.get(propertyName);
        }

        /**
         * Returns true if the name is contained into the properties of the package.
         * @param propertyName Property name.
         * @return True if the property is contained and false in the otherwise.
         */
        public boolean contains(String propertyName) {
            return properties.containsKey(propertyName);
        }

        /**
         * Clean internal properties map.
         */
        public void clear() {
            properties.clear();
        }
    }
}
