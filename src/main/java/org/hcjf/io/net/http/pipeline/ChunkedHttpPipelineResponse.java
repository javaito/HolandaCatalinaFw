package org.hcjf.io.net.http.pipeline;

import org.hcjf.io.net.http.HttpHeader;

import java.nio.ByteBuffer;

/**
 * This specific pipeline implements a chunked http transfer encoding.
 * @author javaito.
 */
public abstract class ChunkedHttpPipelineResponse extends HttpPipelineResponse {

    //Amount of reserved byte for the chunked separators.
    private static final int RESERVED_BYTE_NUMBER = 4;
    private static final byte[] CHUNKED_SEPARATOR = "\r\n".getBytes();
    private boolean end;

    public ChunkedHttpPipelineResponse(int bufferSize) {
        super(calculateMainBufferSize(bufferSize), bufferSize);
        addHeader(new HttpHeader(HttpHeader.TRANSFER_ENCODING, HttpHeader.CHUNKED));
    }

    /**
     * Wrap the buffer with the chunked encoding separators.
     * @param result In this instance of the byte buffer, this method must put
     * the byte to wrap the source data and the source data.
     * @param streamingPackage All the bytes read from the application source.
     * @param size Size of the buffer used to read the application source.
     * @return Returns the amount of bytes read.
     */
    @Override
    protected int wrap(ByteBuffer result, StreamingPackage streamingPackage, int size) {
        int resultSize;
        if(size == -1 && end) {
            resultSize = -1;
        } else {
            result.put(Integer.toString(size > 0 ? size : 0, 16).getBytes());
            result.put(CHUNKED_SEPARATOR);
            result.put(streamingPackage.getBuffer(), 0, size > 0 ? size : 0);
            result.put(CHUNKED_SEPARATOR);
            resultSize = result.position();
            if(size == -1) {
                end = true;
            }
        }
        return resultSize;
    }

    /**
     * This is a utility method to calculate the buffer size necessary to
     * wrap each chunked package.
     * @param bufferSize Chunked package size.
     * @return Returns transfer package size.
     */
    private static int calculateMainBufferSize(int bufferSize) {
        return bufferSize + Integer.toString(bufferSize).getBytes().length + RESERVED_BYTE_NUMBER;
    }
}
