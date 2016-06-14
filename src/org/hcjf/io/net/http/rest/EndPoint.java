package org.hcjf.io.net.http.rest;

import com.sun.istack.internal.NotNull;
import org.hcjf.io.net.http.Context;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by javaito on 1/6/2016.
 */
public class EndPoint extends Context {

    private final String groupName;
    private final String resourceName;
    private final Map<String, Map<String, EndPointImpl>> implementations;

    public EndPoint(String groupName, String resourceName) {
        super("^/" + (groupName == null ? resourceName : (groupName + "/" + resourceName)) + ".*");
        if(resourceName == null) {
            throw new NullPointerException("Resource name can't be null");
        }
        this.groupName = groupName;
        this.resourceName = resourceName;
        this.implementations = new HashMap<>();
    }

    public final void addImplementation(EndPointImpl implementation) {
        String version = implementation.getVersion();
        String format = implementation.getFormat();

        synchronized (implementations) {
            Map<String, EndPointImpl> implMap = implementations.get(version);
            if(implMap == null) {
                implMap = new HashMap<>();
                implementations.put(version, implMap);
            }
            implMap.put(format, implementation);
        }
    }

    protected final String getGroupName() {
        return groupName;
    }

    protected final String getResourceName() {
        return resourceName;
    }

    /**
     * This method is called when there comes a http package addressed to this
     * context.
     *
     * @param request All the request information.
     * @return Return an object with all the response information.
     */
    @Override
    public HttpResponse onContext(HttpRequest request) {
        HttpResponse response;
        String[] paths = request.getContext().split("/");
        int resourceIndex = Arrays.binarySearch(paths, getResourceName());
        if(paths.length > (resourceIndex + 1)) {
            String version = paths[resourceIndex+1];
            String format = version;
            if(paths.length > (resourceIndex + 2)) {
                format = paths[resourceIndex+2];
            }
            EndPointImpl impl = implementations.get(version).get(format);
            if(impl != null) {
                response = impl.onAction(request);
            } else {
                throw new IllegalArgumentException("Illegal request format, implementation not found -> " + request.getContext());
            }
        } else {
            throw new IllegalArgumentException("Illegal request format [GROUP_NAME]/RESOURCE/VERSION/[FORMAT] -> " + request.getContext());
        }
        return response;
    }

    /**
     * This method is called when there are any error on the context execution.
     *
     * @param request   All the request information.
     * @param throwable Throwable object, could be null.
     * @return Return an object with all the response information.
     */
    @Override
    protected HttpResponse onError(HttpRequest request, Throwable throwable) {
        return null;
    }

}
