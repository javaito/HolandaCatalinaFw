package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.layered.LayeredRequest;
import org.hcjf.layers.crud.CrudLayerInterface;

/**
 * This package is the request for the end point implementation interface.
 * @author javaito
 */
public class EndPointRequest extends LayeredRequest {

    private final CrudLayerInterface.CrudInvoker invoker;
    private final Object[] params;

    public EndPointRequest(HttpRequest request, CrudLayerInterface.CrudInvoker invoker, Object[] params) {
        super(request);
        this.invoker = invoker;
        this.params = params;
    }

    /**
     * Return the specific invoker for the current request.
     * @return Crud invoker.
     */
    public final CrudLayerInterface.CrudInvoker getInvoker() {
        return invoker;
    }

    /**
     * Return the parameter instances for the current request.
     * @return Parameter instances.
     */
    public final Object[] getParams() {
        return params;
    }
}
