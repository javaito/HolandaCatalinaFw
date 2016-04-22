package org.hcjf.io.net.http;

import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;

import java.util.UUID;

/**
 * Created by javaito on 18/4/2016.
 */
public class HttpSession extends NetSession<UUID> {

    public HttpSession(UUID sessionId, NetServiceConsumer consumer) {
        super(sessionId, consumer);
    }

}
