package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.layered.LayeredRequest;

/**
 * This package is the request for the end point implementation interface.
 * @author javaito
 */
public abstract class EndPointRequest extends LayeredRequest {

    public EndPointRequest(HttpRequest request) {
        super(request);
    }

}
