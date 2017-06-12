package org.hcjf.io.net.http.layered;

import org.hcjf.io.net.http.Context;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.HttpResponseCode;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.utils.Strings;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This kind of context publish an http interface for
 * som kind of layer.
 * @author javaito
 */
public abstract class LayeredContext<L extends LayerInterface,
        P extends LayeredRequest, R extends LayeredResponse> extends Context {

    private final List<String> endPointPath;

    public LayeredContext(String... endPointPath) {
        super(START_CONTEXT +
                URI_FOLDER_SEPARATOR +
                Strings.join(Arrays.asList(endPointPath), URI_FOLDER_SEPARATOR) +
                END_CONTEXT);
        this.endPointPath = Arrays.asList(endPointPath);
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
     * Return the list of the paths of the end point.
     * @return List of the paths.
     */
    protected final List<String> getEndPointPath() {
        return endPointPath;
    }

    /**
     * Return the list of paths skipping all the end point paths.
     * @param request Http request.
     * @return List of the resource path.
     */
    protected final List<String> getResourcePath(HttpRequest request) {
        return request.getPathParts().stream().skip(getEndPointPath().size()).collect(Collectors.toList());
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
        P layeredRequest = decode(request);
        return encode(onAction(layeredRequest), layeredRequest);
    }

    /**
     * The implementation of this method must resolve the
     * interface with the layer.
     * @param request Request package.
     * @return Response package.
     */
    protected abstract R onAction(P request);

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
        return result;
    }

    /**
     * This implementation must create request package from http request.
     * @param request Http request.
     * @return Layered request package.
     */
    protected abstract P decode(HttpRequest request);

    /**
     * This implementation must create a http response package from
     * response layered package and request layered package.
     * @param response Layered response.
     * @param request Layered request.
     * @return Http response.
     */
    protected abstract HttpResponse encode(R response, P request);

}
