package org.hcjf.io.net.http;

import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;

import java.util.UUID;

/**
 * Net session implementation for the http protocol
 * @author javaito
 */
public class HttpSession extends NetSession {

    private HttpRequest request;

    /**
     * Constructor with id and consumer.
     * @param id Session id.
     * @param consumer Net consumer.
     */
    public HttpSession(UUID id, NetServiceConsumer consumer) {
        super(id, consumer);
    }

    /**
     * Set the request for the session.
     * @param request Http request.
     */
    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    /**
     * Return the session's request.
     * @return Http request.
     */
    public HttpRequest getRequest() {
        return request;
    }
}
