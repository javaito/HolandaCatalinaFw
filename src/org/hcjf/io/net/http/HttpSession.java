package org.hcjf.io.net.http;

import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;

import java.util.UUID;

/**
 * Created by javaito on 18/4/2016.
 */
public class HttpSession extends NetSession {

    private final HttpRequest request;

    public HttpSession(UUID id, String sessionName, NetServiceConsumer consumer, HttpRequest request) {
        super(id, sessionName, consumer);
        this.request = request;
    }

    public HttpRequest getRequest() {
        return request;
    }
}
