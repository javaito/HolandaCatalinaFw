package org.hcjf.io.net.http.rest.layers;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.rest.EndPointRequest;
import org.hcjf.io.net.http.rest.EndPointResponse;
import org.hcjf.layers.LayerInterface;

/**
 * This interface provides the statement to encode the end point result.
 * @author javaito
 */
public interface EndPointEncoderLayerInterface extends LayerInterface {

    /**
     * This method must create a http response object using the information
     * that the crud invocation returns.
     * @param request Invocation request.
     * @param response Invocation result.
     * @return Http response object.
     */
    public HttpResponse encode(EndPointRequest request, EndPointResponse response);

    /**
     * This method must create a http response object using the throwable information.
     * @param request Invocation request.
     * @param throwable Throwable information.
     * @return Http response object.
     */
    public HttpResponse encode(HttpRequest request, Throwable throwable);

}
