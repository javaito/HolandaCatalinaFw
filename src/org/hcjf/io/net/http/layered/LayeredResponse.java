package org.hcjf.io.net.http.layered;

import org.hcjf.io.net.http.HttpResponse;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class LayeredResponse extends HttpResponse {

    private final Object layerResponse;

    public LayeredResponse(Object layerResponse) {
        this.layerResponse = layerResponse;
    }

    public final Object getLayerResponse() {
        return layerResponse;
    }

}
