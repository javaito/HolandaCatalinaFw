package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.layered.LayeredContext;
import org.hcjf.io.net.http.rest.layers.EndPointDecoderLayerInterface;
import org.hcjf.io.net.http.rest.layers.EndPointEncoderLayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.layers.query.Query;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * This context publish some kind of layer that response with rest interface.
 * @author javaito
 */
public class EndPoint extends LayeredContext<CrudLayerInterface, EndPointRequest, EndPointResponse> {

    public EndPoint(String... endPointPath) {
        super(endPointPath);
    }

    /**
     * This method call the invoker stored into the end point request over the crud
     * layer using the the first name of the custom path into the url after the basic path
     * of the context.
     * @param request Request package.
     * @return End point response package.
     */
    @Override
    protected final EndPointResponse onAction(EndPointRequest request) {
        EndPointResponse result;
        if(request instanceof EndPointCrudRequest) {
            try {
                EndPointCrudRequest crudRequest = (EndPointCrudRequest) request;
                result = new EndPointResponse(crudRequest.getInvoker().invoke(crudRequest.getCrudLayerInterface(), crudRequest.getParams()));
            } catch (InvocationTargetException e) {
                throw new RuntimeException("", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("", e);
            }
        } else if(request instanceof EndPointQueryRequest) {
            EndPointQueryRequest queryRequest = (EndPointQueryRequest) request;
            result = new EndPointResponse(Query.evaluate(queryRequest.getQuery()));
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    /**
     * If the request has an accept header then this method must encode the error information based
     * on the encoder layer associated to the accept header value as a layer name. If the request
     * has not an accept header then is called the super implementation method.
     * @param request   All the request information.
     * @param throwable Throwable object, could be null.
     * @return Http response package with the error information.
     */
    @Override
    protected final HttpResponse onError(HttpRequest request, Throwable throwable) {
        HttpResponse response;
        HttpHeader acceptHeader = request.getHeader(HttpHeader.ACCEPT);
        if(acceptHeader == null) {
            response = super.onError(request, throwable);;
        } else {
            EndPointEncoderLayerInterface decoderLayerInterface =
                    Layers.get(EndPointEncoderLayerInterface.class, acceptHeader.getHeaderValue());
            response = decoderLayerInterface.encode(request, throwable);
        }
        return response;
    }

    /**
     * This method decode the http request using some implementation of the
     * {@link EndPointDecoderLayerInterface}, the implementation name is the value of the
     * header content-type.
     * @param request Http request.
     * @return End point request package.
     * @throws IllegalArgumentException If the request has not contains the content-type
     * header then this exception is throws.
     */
    @Override
    protected final EndPointRequest decode(HttpRequest request) {
        EndPointRequest result;

        if(request.getParameters().containsKey("q")) {
            result = new EndPointQueryRequest(request, Query.compile(request.getParameter("q")));
        } else {
            List<String> resourcePath = getResourcePath(request);
            if (resourcePath.isEmpty()) {
                throw new IllegalArgumentException("");
            }

            CrudLayerInterface crudLayerInterface = getLayerInterface(resourcePath.stream().findFirst().get());

            HttpHeader contentTypeHeader = request.getHeader(HttpHeader.CONTENT_TYPE);
            if (contentTypeHeader == null) {
                throw new IllegalArgumentException("");
            }

            EndPointDecoderLayerInterface endPointDecoderLayerInterface =
                    Layers.get(EndPointDecoderLayerInterface.class, contentTypeHeader.getHeaderValue());

            result = endPointDecoderLayerInterface.decode(request, crudLayerInterface);
        }

        return result;
    }

    /**
     * This method encode the http request using some implementation of the
     * {@link EndPointEncoderLayerInterface}, the implementation name is the value of the
     * header accept.
     * @param response Layered response.
     * @param request Layered request.
     * @return Http response package.
     * @throws IllegalArgumentException If the request has not contains the accept
     * header then this exception is throws.
     */
    @Override
    protected final HttpResponse encode(EndPointResponse response, EndPointRequest request) {
        HttpHeader acceptHeader = request.getHeader(HttpHeader.ACCEPT);
        if(acceptHeader == null) {
            throw new IllegalArgumentException("");
        }

        EndPointEncoderLayerInterface decoderLayerInterface =
                Layers.get(EndPointEncoderLayerInterface.class, acceptHeader.getHeaderValue());
        return decoderLayerInterface.encode(request, response);
    }

}
