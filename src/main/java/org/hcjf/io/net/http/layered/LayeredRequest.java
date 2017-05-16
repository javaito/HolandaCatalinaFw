package org.hcjf.io.net.http.layered;

import org.hcjf.io.net.http.HttpRequest;

/**
 * This class represents a package that contains all the
 * information about a restful request.
 * @author javaito
 */
public class LayeredRequest extends HttpRequest {

    public LayeredRequest(HttpRequest request) {
        super(request);
    }

}
