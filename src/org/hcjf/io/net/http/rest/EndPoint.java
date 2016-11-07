package org.hcjf.io.net.http.rest;

import org.hcjf.errors.Errors;
import org.hcjf.io.net.http.HttpMethod;
import org.hcjf.io.net.http.layered.*;
import org.hcjf.layers.LayerInterface;

/**
 * 
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class EndPoint<L extends LayerInterface,
        P extends EndPointRequest, R extends EndPointResponse> extends LayeredContext<L, P, R> {

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
    public final R onAction(P request) {
        R result = null;
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
    protected R get(P layeredRequest) {
        throw new UnsupportedOperationException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_REST_4, HttpMethod.GET.toString()));
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    protected R post(P layeredRequest) {
        throw new UnsupportedOperationException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_REST_4, HttpMethod.POST.toString()));
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    protected R put(P layeredRequest) {
        throw new UnsupportedOperationException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_REST_4, HttpMethod.PUT.toString()));
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    protected R delete(P layeredRequest) {
        throw new UnsupportedOperationException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_REST_4, HttpMethod.DELETE.toString()));
    }

}
