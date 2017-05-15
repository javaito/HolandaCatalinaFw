package org.hcjf.io.net.http.rest.layers;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.rest.EndPointRequest;
import org.hcjf.layers.LayerInterface;

/**
 * This layer interface provides the statement to decode a http request and
 * create a end point package.
 * @author Javier Quiroga.
 */
public interface EndPointDecoderLayerInterface extends LayerInterface {

    /**
     * The implementation of this method must create a end point package
     * using the http request information.
     * @param request Http request.
     * @return End point package.
     */
    public EndPointRequest decode(HttpRequest request);

}
