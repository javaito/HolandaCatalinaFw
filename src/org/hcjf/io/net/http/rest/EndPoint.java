package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.layered.*;
import org.hcjf.layers.LayerInterface;

/**
 * 
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class EndPoint<L extends LayerInterface,
        P extends LayeredRequest, R extends LayeredResponse> extends LayeredContext<L, P, R> {

    public EndPoint(String groupName, String resourceName) {
        super(groupName, resourceName);
    }

    /**
     * This method is called when there comes a http package addressed to this
     * context.
     *
     * @param request All the request information.
     * @return Return an object with all the response information.
     */
    @Override
    public final Object onAction(P request) {
        Object result = null;
        switch (request.getMethod()) {
            case GET: {
                result = get(request);
                break;
            }
            case POST: {
                result = post(request);
                break;
            }
            case PUT: {
                result = put(request);
                break;
            }
            case DELETE: {
                result = delete(request);
                break;
            }
        }

        return result;
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    protected Object get(P layeredRequest) {
        throw new UnsupportedOperationException("GET method is not implemented on the REST interface");
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    protected Object post(P layeredRequest) {
        throw new UnsupportedOperationException("POST method is not implemented on the REST interface");
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    protected Object put(P layeredRequest) {
        throw new UnsupportedOperationException("PUT method is not implemented on the REST interface");
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    protected Object delete(P layeredRequest) {
        throw new UnsupportedOperationException("DELETE method is not implemented on the REST interface");
    }

}
