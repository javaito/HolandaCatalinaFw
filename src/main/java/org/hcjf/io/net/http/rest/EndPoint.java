package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.layered.LayeredContext;
import org.hcjf.io.net.http.rest.layers.EndPointDecoderLayerInterface;
import org.hcjf.io.net.http.rest.layers.EndPointEncoderLayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.CrudLayerInterface;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * This context publish some kind of layer that response with rest interface.
 * @author javaito
 */
public abstract class EndPoint extends LayeredContext<CrudLayerInterface, EndPointRequest, EndPointResponse> {

    public EndPoint(String... endPointPath) {
        super(endPointPath);
    }

    @Override
    protected final EndPointResponse onAction(EndPointRequest request) {
        List<String> resourcePath = getResourcePath(request);
        if(resourcePath.isEmpty()) {
            throw new IllegalArgumentException("");
        }

        CrudLayerInterface crudLayerInterface = getLayerInterface(resourcePath.get(0));
        try {
            return new EndPointResponse(request.getInvoker().invoke(crudLayerInterface, request.getParams()));
        } catch (InvocationTargetException e) {
            throw new RuntimeException("", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("", e);
        }
    }

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

    @Override
    protected final EndPointRequest decode(HttpRequest request) {
        HttpHeader contentTypeHeader = request.getHeader(HttpHeader.CONTENT_TYPE);
        if(contentTypeHeader == null) {
            throw new IllegalArgumentException("");
        }

        EndPointDecoderLayerInterface endPointDecoderLayerInterface =
                Layers.get(EndPointDecoderLayerInterface.class, contentTypeHeader.getHeaderValue());

        return endPointDecoderLayerInterface.decode(request);
    }

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
