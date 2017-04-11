package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.layered.LayeredRequest;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class EndPointRequest extends LayeredRequest {

    public EndPointRequest(HttpRequest request) {
        super(request);
    }

}
