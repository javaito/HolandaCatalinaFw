package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.layers.crud.CrudLayerInterface;

/**
 * @author javaito
 */
public class EndPointCrudRequest extends EndPointRequest {

    private final CrudLayerInterface.CrudInvoker invoker;
    private final Object[] params;
    private final CrudLayerInterface crudLayerInterface;

    public EndPointCrudRequest(HttpRequest request, CrudLayerInterface crudLayerInterface,
                               CrudLayerInterface.CrudInvoker invoker, Object... params) {
        super(request);
        this.invoker = invoker;
        this.params = params;
        this.crudLayerInterface = crudLayerInterface;
    }

    /**
     * Return the layer to invoke.
     * @return Crud layer instance.
     */
    public final CrudLayerInterface getCrudLayerInterface() {
        return crudLayerInterface;
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
