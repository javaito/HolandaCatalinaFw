package org.hcjf.io.net.http.view;

import org.hcjf.io.net.http.layered.LayeredContext;
import org.hcjf.layers.view.ViewCrudLayerInterface;
import org.hcjf.view.ViewComponent;

/**
 * @mail armedina@gmail.com
 */
public abstract class ViewContext<L extends ViewCrudLayerInterface,
        P extends ViewRequest, R extends ViewResponse> extends LayeredContext<L, P, R> {

    public ViewContext(String groupName, String resourceName) {
        super(groupName, resourceName);
    }

    @Override
    protected final R onAction(P request) {
        R result = null;
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

    private R onViewAction(P request) {
        L layer = getLayerInterface(request.getResourceName());
        return createViewResponse(layer.onAction(request.getAction(),request.getViewParameters()));
    }

    protected abstract R createViewResponse(ViewComponent component);
}
