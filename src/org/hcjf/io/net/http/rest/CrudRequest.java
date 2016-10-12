package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.layered.LayeredRequest;

import java.util.Map;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class CrudRequest extends EndPointRequest {

    private final Map<String, Object> crudParameters;
    private final Object attach;
    private final String resourceName;
    private final String resourceAction;
    private final String id;

    public CrudRequest(HttpRequest request,
                          Map<String, Object> crudParameters,
                          Object attach,
                          String resourceName,
                          String resourceAction,
                          String id) {
        super(request);
        this.crudParameters = crudParameters;
        this.attach = attach;
        this.resourceName = resourceName;
        this.resourceAction = resourceAction;
        this.id = id;
    }

    /**
     *
     * @return
     */
    public Object getAttach() {
        return attach;
    }

    /**
     *
     * @return
     */
    public Map<String, Object> getCrudParameters() {
        return crudParameters;
    }

    /**
     *
     * @return
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     *
     * @return
     */
    public String getResourceAction() {
        return resourceAction;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

}
