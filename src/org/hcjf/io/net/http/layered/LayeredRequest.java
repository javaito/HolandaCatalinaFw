package org.hcjf.io.net.http.layered;

import org.hcjf.io.net.http.HttpRequest;

import java.util.*;

/**
 * This class represents a package that contains all the
 * information about a restful request.
 * @author javaito
 * @email javaito@gmail.com
 */
public class LayeredRequest extends HttpRequest {

    private final Map<String, Object> layerParameters;
    private final Object attach;
    private final String resourceName;
    private final String resourceAction;
    private final String id;

    public LayeredRequest(HttpRequest request,
                          Map<String, Object> layerParameters,
                          Object attach,
                          String resourceName,
                          String resourceAction,
                          String id) {
        super(request);
        this.layerParameters = layerParameters;
        this.attach = attach;
        this.resourceName = resourceName;
        this.resourceAction = resourceAction;
        this.id = id;
    }

    /**
     *
     * @return
     */
    public Object getAttach() {
        return attach;
    }

    /**
     *
     * @return
     */
    public Map<String, Object> getLayerParameters() {
        return layerParameters;
    }

    /**
     *
     * @return
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     *
     * @return
     */
    public String getResourceAction() {
        return resourceAction;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }
}
