package org.hcjf.io.net.proxy;

import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;

import java.util.UUID;

public class TcpProxySession extends NetSession {

    private final String host;
    private final Boolean client;
    private final Boolean main;

    public TcpProxySession(UUID id, NetServiceConsumer consumer, String host) {
        super(id, consumer);
        this.host = host;
        this.client = false;
        this.main = false;
    }

    public TcpProxySession(UUID id, NetServiceConsumer consumer, String host, Boolean client, Boolean main) {
        super(id, consumer);
        this.host = host;
        this.client = client;
        this.main = main;
    }

    public String getHost() {
        return host;
    }

    public Boolean getClient() {
        return client;
    }

    public Boolean getMain() {
        return main;
    }
}
