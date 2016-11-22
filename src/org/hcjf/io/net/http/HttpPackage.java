package org.hcjf.io.net.http;

import org.hcjf.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * This class represents all the king of packages between server and
 * client side in a http comunication.
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class HttpPackage {

    private static final byte LINE_SEPARATOR_CR = '\r';
    private static final byte LINE_SEPARATOR_LF = '\n';
    public static final String STRING_LINE_SEPARATOR = "\r\n";
    public static final String LINE_FIELD_SEPARATOR = " ";
    public static final String HTTP_FIELD_START = "?";
    public static final String HTTP_FIELD_SEPARATOR = "&";
    public static final String HTTP_FIELD_ASSIGNATION = "=";
    public static final String HTTP_CONTEXT_SEPARATOR = "/";

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
    }

    protected HttpPackage(HttpPackage httpPackage) {
        this.httpVersion = httpPackage.httpVersion;
        this.headers = httpPackage.headers;
        this.body = httpPackage.getBody();
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
     *
     * @return
     */
    public final String getHttpVersion() {
        return httpVersion;
    }

    /**
     *
     * @param httpVersion
     */
    public final void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    /**
     *
     * @return
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
     *
     * @param headerName
     * @return
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
     *
     * @param headerName
     * @return
     */
    public final boolean containsHeader(String headerName) {
        return getHeader(headerName) != null;
    }

    /**
     *
     * @param data
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
                    currentBody.write(data);
                } catch (IOException ex) {
                }
            } else {
                String line;
                for (int i = 0; i < data.length - 1; i++) {
                    if (data[i] == LINE_SEPARATOR_CR && data[i + 1] == LINE_SEPARATOR_LF) {
                        if (currentBody.size() == 0) {
                            //The previous line is empty
                            //Start body, because there are two CRLF together
                            currentBody.reset();
                            currentBody.write(data, i + 2, data.length - (i + 2));
                            onBody = true;
                            for(int j = 1; j < lines.size(); j++) {
                                addHeader(new HttpHeader(lines.get(j)));
                            }
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
                int length = 0;
                HttpHeader contentLengthHeader = getHeader(HttpHeader.CONTENT_LENGTH);
                if (contentLengthHeader != null) {
                    length = Integer.parseInt(contentLengthHeader.getHeaderValue().trim());
                }

                if (currentBody.size() >= length) {
                    setBody(currentBody.toByteArray());
                    processFirstLine(lines.get(0));
                    processBody(getBody());
                    currentBody = null;
                    complete = true;
                }
            }
        } else {
            Log.d(HttpServer.HTTP_SERVER_LOG_TAG, "Trying to add data into a complete http package.");
        }
    }

    /**
     *
     * @param body
     * @return
     */
    protected abstract void processBody(byte[] body);

    /**
     *
     * @param firstLine
     */
    protected abstract void processFirstLine(String firstLine);

    /**
     *
     * @return
     */
    public abstract byte[] getProtocolHeader();

}
