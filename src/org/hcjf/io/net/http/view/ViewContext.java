package org.hcjf.io.net.http.view;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.layered.LayeredContext;
import org.hcjf.io.net.http.layered.LayeredRequest;
import org.hcjf.io.net.http.layered.LayeredResponse;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.view.ViewLayerInterface;

/**
 * @mail armedina@gmail.com
 */
public class ViewContext<L extends LayerInterface> extends LayeredContext<L> {

    public ViewContext(String groupName, String resourceName) {
        super(groupName, resourceName);
    }

    @Override
    protected Object onAction(LayeredRequest request) {
        Object result = null;
        result = get(request);
        return result;
    }

    protected Object get(LayeredRequest layeredRequest) {
        throw new UnsupportedOperationException("GET method is not implemented on the REST interface");
    }

    @Override
    protected LayeredRequest decode(HttpRequest request) {
        String context = request.getContext();
        String resource =  context.substring(context.lastIndexOf("/")+1);
        LayeredRequest layeredRequest = new LayeredRequest(
                request,
                null,
                null,
                resource,
                request.getMethod().name(),
                "");
        return  layeredRequest;
    }

    @Override
    protected LayeredResponse encode(Object object, LayeredRequest request) {
        return null;
    }
}
