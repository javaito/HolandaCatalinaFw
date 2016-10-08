package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.layered.LayeredResponse;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class EndPointResponse extends LayeredResponse {

    public EndPointResponse(HttpResponse httpResponse) {
        super(httpResponse);
    }

}
