package org.hcjf.io.net.http;

import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;

import java.util.UUID;

/**
 * Created by javaito on 18/4/2016.
 */
public class HttpSession extends NetSession<UUID> {

    private final HttpRequest request;

    public HttpSession(NetServiceConsumer consumer, HttpRequest request) {
        super(UUID.randomUUID(), consumer);
        this.request = request;
    }

    public HttpRequest getRequest() {
        return request;
    }
}
