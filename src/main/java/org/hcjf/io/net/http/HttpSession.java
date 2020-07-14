package org.hcjf.io.net.http;

import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;
import org.hcjf.io.net.http.http2.Stream;

import java.util.UUID;

/**
 * Net session implementation for the http protocol
 * @author javaito
 */
public class HttpSession extends NetSession {

    private HttpRequest request;
    private Stream stream;
    private String httpVersion;

    /**
     * Constructor with id and consumer.
     * @param id Session id.
     * @param consumer Net consumer.
     */
    public HttpSession(UUID id, NetServiceConsumer consumer) {
        super(id, consumer);
        this.httpVersion = HttpVersion.VERSION_1_1;
    }

    protected HttpSession(HttpSession httpSession) {
        super(httpSession);
        this.request = httpSession.request;
        this.stream = httpSession.stream;
    }

    /**
     * Set the request for the session.
     * @param request Http request.
     */
    public final void setRequest(HttpRequest request) {
        this.request = request;
    }

    /**
     * Return the session's request.
     * @return Http request.
     */
    public final HttpRequest getRequest() {
        return request;
    }

    /**
     * Returns the http2 stream object associated to the session.
     * @return Http2 stream object.
     */
    public final Stream getStream() {
        return stream;
    }

    /**
     * Set the http2 stream object associated to the session.
     * @param stream Http2 stream object.
     */
    public final void setStream(Stream stream) {
        this.stream = stream;
    }

    /**
     * Set the http protocol version that the session use.
     * @return Http version of the session.
     */
    public final String getHttpVersion() {
        return httpVersion;
    }

    /**
     * Returns the http protocol version that the session use.
     * @param httpVersion Protocol version of the session.
     */
    public final void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }
}
