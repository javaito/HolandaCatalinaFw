package org.hcjf.io.net.http;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * This class represents all the king of packages between server and
 * client side in a http communication.
 * @author javaito
 */
public abstract class HttpPackage {

    private static BodyWriter bodyWriter;
    private static final byte LINE_SEPARATOR_CR = '\r';
    private static final byte LINE_SEPARATOR_LF = '\n';
    public static final String STRING_LINE_SEPARATOR = "\r\n";
    public static final String LINE_FIELD_SEPARATOR = " ";
    public static final String HTTP_FIELD_START = "?";
    public static final String HTTP_FIELD_SEPARATOR = "&";
    public static final String HTTP_FIELD_ASSIGNATION = "=";
    public static final String HTTP_CONTEXT_SEPARATOR = "/";

    private HttpProtocol protocol;
    private String httpVersion;
    private final Map<String, HttpHeader> headers;
    private byte[] body;

    //This fields are only for internal parsing.
    private List<String> lines;
    private boolean onBody;
    private boolean complete;
    private ByteArrayOutputStream currentBody;

    public HttpPackage() {
        this.httpVersion = HttpVersion.VERSION_1_1;
        this.headers = new HashMap<>();
        this.body = new byte[0];
        this.protocol = HttpProtocol.HTTP;
    }

    protected HttpPackage(HttpPackage httpPackage) {
        this.httpVersion = httpPackage.httpVersion;
        this.headers = httpPackage.headers;
        this.body = httpPackage.body;
        this.protocol = httpPackage.protocol;
    }

    /**
     * Return the http protocol.
     * @return Http protocol
     */
    public HttpProtocol getProtocol() {
        return protocol;
    }

    /**
     * Set the http protocol.
     * @param protocol Http protocol
     */
    public void setProtocol(HttpProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Return the body of the package.
     * @return Body.
     */
    public final byte[] getBody() {
        return body;
    }

    /**
     * Set the body of the package.
     * @param body Body.
     */
    public final void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * Return the version of the http protocol
     * @return Http protocol version.
     */
    public final String getHttpVersion() {
        return httpVersion;
    }

    /**
     * Set the version of the http protocol
     * @param httpVersion Version of the http protocol
     */
    public final void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    /**
     * Return true if the http package is complete.
     * @return Trus if the package is complete and false in the otherwise.
     */
    public final boolean isComplete() {
        return complete;
    }

    /**
     * Add a new header into a package.
     * @param header New header.
     */
    public final void addHeader(HttpHeader header) {
        if(header == null) {
            throw new NullPointerException("Null header");
        }
        headers.put(header.getHeaderName(), header);
    }

    /**
     * Return in a unmodificable list, all the headers contained
     * into the package.
     * @return List of the headers.
     */
    public final Collection<HttpHeader> getHeaders() {
        return Collections.unmodifiableCollection(headers.values());
    }

    /**
     * Return the header instance with the specific name.
     * @param headerName Name of the founded header.
     * @return Founded header or null if there are'nt any header with this name.
     */
    public final HttpHeader getHeader(String headerName) {
        HttpHeader result = null;
        for(String name : headers.keySet()) {
            if(name.equalsIgnoreCase(headerName)) {
                result = headers.get(name);
            }
        }
        return result;
    }

    /**
     * Verify if the package contains any header with the specific name.
     * @param headerName Finding header name.
     * @return Return true if there are any header with the specific name and false in the otherwise.
     */
    public final boolean containsHeader(String headerName) {
        return getHeader(headerName) != null;
    }

    /**
     * Add a portion of data into the package.
     * @param data Portion of data.
     */
    public final synchronized void addData(byte[] data) {
        if(!complete) {
            if (currentBody == null) {
                currentBody = new ByteArrayOutputStream();
                lines = new ArrayList<>();
                onBody = false;
                complete = false;
            }

            if (onBody) {
                try {
                    bodyWriter.write(data);
                } catch (IOException ex) {
                }
            } else {
                String line;
                for (int i = 0; i < data.length - 1; i++) {
                    if (data[i] == LINE_SEPARATOR_CR && data[i + 1] == LINE_SEPARATOR_LF) {
                        if (currentBody.size() == 0) {
                            onBody = true;
                            for(int j = 1; j < lines.size(); j++) {
                                addHeader(new HttpHeader(lines.get(j)));
                            }
                            HttpHeader transferEncodingHeader = getHeader(HttpHeader.TRANSFER_ENCODING);
                            if(transferEncodingHeader != null && transferEncodingHeader.getHeaderValue().equals(HttpHeader.CHUNKED)){
                                bodyWriter = new ChunkedBodyWriter();
                            }else{
                                bodyWriter = new StandardBodyWriter();
                            }
                            //The previous line is empty
                            //Start body, because there are two CRLF together
                            currentBody.reset();
                            bodyWriter.write(data, i + 2, data.length - (i + 2));
                            break;
                        } else {
                            //The current body is a new line
                            line = new String(currentBody.toByteArray()).trim();
                            if(!line.isEmpty()) {
                                lines.add(line);
                            }
                            currentBody.reset();
                            i++;
                        }
                    } else {
                        currentBody.write(data[i]);
                    }
                }
            }

            if (onBody) {
                if (bodyWriter.isComplete()) {
                    setBody(currentBody.toByteArray());
                    processFirstLine(lines.get(0));
                    processBody(getBody());
                    currentBody = null;
                    complete = true;
                }
            }
        } else {
            Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Trying to add data into a complete http package.");
        }
    }

    /**
     * This method must be implemented to process the body information.
     * @param body Body information.
     */
    protected abstract void processBody(byte[] body);

    /**
     * This method must be implemented to process the first line of the package.
     * @param firstLine First line of the package.
     */
    protected abstract void processFirstLine(String firstLine);

    /**
     * Return the bytes that represent the string of the protocol name.
     * @return Protocol name bytes.
     */
    public abstract byte[] getProtocolHeader();

    /**
     * Enum with the http protocols
     */
    public enum HttpProtocol {

        HTTP,

        HTTPS

    }

    /**
     * Implement this interface to handle different types of body encodings
     */
    private interface BodyWriter{
        /**
         * Write len bytes in the body
         * @param data the data
         * @throws IOException
         */
        void write(byte[] data) throws IOException;

        /**
         * Writes len bytes from the specified byte array starting at offset off
         * to this byte array output stream.
         * @param data the data
         * @param offset the start offset in the data
         * @param length the number of bytes to write
         */
        void write(byte[] data, int offset, int length);

        /**
         * @return true if everything has been received
         */
        boolean isComplete();
    }

    /**
     * BodyWriter class for chunked type transfer-encoding
     */
    private class ChunkedBodyWriter implements BodyWriter{
        private boolean lastChunkReceived = false;
        private int dataToRead = 0;
        private int nextChunkSizeIndex = 0;

        /**
         * {@inheritDoc}
         */
        public void write(byte[] data){
            write(data, 0, data.length);
        }

        /**
         * {@inheritDoc}
         */
        public void write(byte[] data, int offset, int length){
            nextChunkSizeIndex += offset;
            length += offset;

            for(int i = offset+1; i < length; i++){
                if(dataToRead > 0){
                    currentBody.write(data[i]);
                    dataToRead--;
                } else if(nextChunkSizeIndex < i && data[i-1] == LINE_SEPARATOR_CR && data[i] == LINE_SEPARATOR_LF){
                    //Read the chunk size and parse to integer
                    dataToRead = numeric(data, nextChunkSizeIndex,i-1);

                    if(dataToRead == 0){
                        lastChunkReceived = true;
                        return;
                    }
                    nextChunkSizeIndex = i /* actual position */ + 1 /* \n */ + dataToRead + 2 /* \r\n */;
                }
            }
            // If there are more chunks the index position is changed to the following data array
            nextChunkSizeIndex -= data.length;
        }

        /**
         * Parse hexadecimal bytes from the specified byte array starting at offset off
         * to the equivalent int.
         * <br>
         * Adapted from {@link sun.net.httpserver.ChunkedInputStream}
         *
         * @param data the data
         * @param offset the start offset in the data
         * @param length the number of bytes to write
         * @return the int value
         */
        private int numeric(byte[] data, int offset, int length){
            int result = 0;

            for(int i = offset; i < length; ++i) {
                byte character = data[i];
                int value;
                if(character >= 48 && character <= 57) {
                    value = character - 48;
                } else if(character >= 97 && character <= 102) {
                    value = character - 97 + 10;
                } else {
                    if(character < 65 || character > 70) {
                        throw new RuntimeException("invalid chunk length");
                    }
                    value = character - 65 + 10;
                }
                result = result * 16 + value;
            }
            return result;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isComplete(){
            return lastChunkReceived;
        }
    }

    /**
     * BodyWriter class for standard encoding
     */
    private class StandardBodyWriter implements BodyWriter{
        /**
         * {@inheritDoc}
         */
        public void write(byte[] data) throws IOException{
            write(data, 0, data.length);
        }

        /**
         * {@inheritDoc}
         */
        public void write(byte[] data, int offset, int length){
            currentBody.write(data, offset, length);
        }

        /**
         * {@inheritDoc}
         */
        public boolean isComplete(){
            int length = 0;
            HttpHeader contentLengthHeader = getHeader(HttpHeader.CONTENT_LENGTH);
            if (contentLengthHeader != null) {
                length = Integer.parseInt(contentLengthHeader.getHeaderValue().trim());
            }
            return currentBody.size() >= length;
        }
    }

}
