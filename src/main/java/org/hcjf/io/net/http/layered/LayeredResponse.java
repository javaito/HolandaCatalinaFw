package org.hcjf.io.net.http.layered;

import org.hcjf.io.net.http.HttpResponse;

/**
 * Response package for the layered contexts.
 * @author javaito
 */
public class LayeredResponse extends HttpResponse {

    private final Object layerResponse;

    public LayeredResponse(Object layerResponse) {
        this.layerResponse = layerResponse;
    }

    /**
     * Get the response object of the layer's invocation
     * @return Response object.
     */
    public final Object getLayerResponse() {
        return layerResponse;
    }

}
