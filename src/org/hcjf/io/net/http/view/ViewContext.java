package org.hcjf.io.net.http.view;

import org.hcjf.io.net.http.layered.LayeredContext;
import org.hcjf.io.net.http.layered.LayeredRequest;
import org.hcjf.io.net.http.layered.LayeredResponse;
import org.hcjf.layers.view.ViewCrudLayerInterface;
import org.hcjf.layers.view.ViewLayerInterface;

/**
 * @mail armedina@gmail.com
 */
public abstract class ViewContext<L extends ViewLayerInterface,
        P extends ViewRequest, R extends ViewResponse> extends LayeredContext<L, P, R> {

    public ViewContext(String groupName, String resourceName) {
        super(groupName, resourceName);
    }

    @Override
    protected final Object onAction(P request) {
        Object result = null;
        switch (request.getMethod()) {
            case GET: {
                result = onViewAction(request);
                break;
            }
            case POST:
            case PUT:
            case DELETE: {
                throw new UnsupportedOperationException("Method is not implemented on the VIEW interface");
            }
        }
        return result;
    }

    private Object onViewAction(P request) {
        L layer = getLayerInterface(request.getResourceName());
        return layer.onAction(request.getAction(),request.getViewParameters());
    }

}
