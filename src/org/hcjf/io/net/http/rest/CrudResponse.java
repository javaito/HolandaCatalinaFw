package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.layered.LayeredResponse;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class CrudResponse extends LayeredResponse {

    public CrudResponse(HttpResponse httpResponse) {
        super(httpResponse);
    }
    
}
