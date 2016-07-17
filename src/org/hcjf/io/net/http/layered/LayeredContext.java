package org.hcjf.io.net.http.layered;

import org.hcjf.io.net.http.*;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;

import java.lang.reflect.ParameterizedType;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class LayeredContext<L extends LayerInterface> extends Context {

    private final String layerGroupName;
    private final String resourceName;

    public LayeredContext(String layerGroupName, String resourceName) {
        super("^/" + (layerGroupName == null ? resourceName : (layerGroupName + "/" + resourceName)) + ".*");
        if(resourceName == null) {
            throw new NullPointerException("Resource name can't be null");
        }
        this.layerGroupName = layerGroupName;
        this.resourceName = resourceName;
    }

    /**
     * Return the name of the group
     * @return Group's name.
     */
    public String getLayerGroupName() {
        return layerGroupName;
    }

    /**
     * Return the name of the resource.
     * @return Resource's name.
     */
    protected final String getResourceName() {
        return resourceName;
    }

    /**
     * This method must return the instance of the layer interface.
     * @param implementationName Name of the implementation founded
     * @return Return the implementation founded.
     * @throws IllegalArgumentException if the implementation with the
     * pointed name is not found.
     */
    protected final L getLayerInterface(String implementationName) {
        Class<L> implementationClass = (Class<L>)
                ((ParameterizedType)getClass().getGenericSuperclass()).
                        getActualTypeArguments()[0];
        return Layers.get(implementationClass, implementationName);
    }

    /**
     * This method is called when there comes a http package addressed to this
     * context.
     *
     * @param request All the request information.
     * @return Return an object with all the response information.
     */
    @Override
    public final HttpResponse onContext(HttpRequest request) {
        LayeredRequest layeredRequest = decode(request);
        Object actionResponse = onAction(layeredRequest);
        return encode(actionResponse, layeredRequest);
    }

    /**
     *
     * @param request
     * @return
     */
    protected abstract Object onAction(LayeredRequest request);

    /**
     * This method is called when there are any error on the context execution.
     *
     * @param request   All the request information.
     * @param throwable Throwable object, could be null.
     * @return Return an object with all the response information.
     */
    @Override
    protected HttpResponse onError(HttpRequest request, Throwable throwable) {
        HttpResponse result = new HttpResponse();
        result.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
        result.setReasonPhrase(throwable.getMessage());
        return result;
    }

    /**
     * @param request
     * @return
     */
    protected abstract LayeredRequest decode(HttpRequest request);

    /**
     * @param object
     * @param request
     * @return
     */
    protected abstract LayeredResponse encode(Object object, LayeredRequest request);
}
