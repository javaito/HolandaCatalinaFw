package org.hcjf.io.net.http;

import org.hcjf.layers.LayerInterface;

import java.util.Map;

/**
 * This class defines the interface to write the http request
 * body parser for each content type.
 * @author javaito
 */
public interface RequestBodyDecoderLayer extends LayerInterface {

    /**
     * Returns a map with the parameters parsed from the request body.
     * @param request Http request instance.
     * @return Map with all the parameters.
     */
    Map<String, Object> decode(HttpRequest request);

}
