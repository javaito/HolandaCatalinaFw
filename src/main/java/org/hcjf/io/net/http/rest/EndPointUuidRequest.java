package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpRequest;

import java.util.UUID;

/**
 * @author javaito
 */
public class EndPointUuidRequest extends EndPointRequest {

    private final UUID uuid;

    public EndPointUuidRequest(HttpRequest request, UUID uuid) {
        super(request);
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
