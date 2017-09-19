package org.hcjf.io.net.http.pipeline;

import org.hcjf.io.net.http.HttpHeader;

import java.nio.ByteBuffer;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public abstract class MultipartHttpPipelineResponse extends HttpPipelineResponse {

    private static final byte[] END = "--".getBytes();
    private static final byte[] MULTIPART_BOUNDARY_START = "\r\n--".getBytes();
    private static final byte[] HEADER_SEPARATOR = "\r\n".getBytes();

    private final String boundary;
    private boolean end;

    public MultipartHttpPipelineResponse(int mainBufferSize, int bufferSize, String boundary) {
        super(mainBufferSize, bufferSize);
        this.boundary = boundary;
    }

    @Override
    protected int wrap(ByteBuffer result, StreamingPackage streamingPackage, int size) {
        int resultSize;
        if(size == -1 && end) {
            resultSize = -1;
        } else {
            result.put(MULTIPART_BOUNDARY_START);
            result.put(boundary.getBytes());
            result.put(HEADER_SEPARATOR);
            if(streamingPackage.contains(HttpHeader.CONTENT_DISPOSITION)) {
                result.put(streamingPackage.get(HttpHeader.CONTENT_DISPOSITION).toString().getBytes());
                result.put(HEADER_SEPARATOR);
            } else if(streamingPackage.contains(HttpHeader.CONTENT_TYPE)) {
                result.put(streamingPackage.get(HttpHeader.CONTENT_TYPE).toString().getBytes());
                result.put(HEADER_SEPARATOR);
            }
            if(size == -1) {
                end = true;
                result.put(END);
            } else {
                result.put(streamingPackage.getBuffer(), 0, size);
            }
            resultSize = result.position();
        }

        return resultSize;
    }


}
