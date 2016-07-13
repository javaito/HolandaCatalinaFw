package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.layered.LayeredRequest;
import org.hcjf.io.net.http.layered.LayeredResponse;
import org.hcjf.layers.Query;
import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.encoding.DecodedPackage;
import org.hcjf.encoding.MimeType;
import org.hcjf.encoding.EncodingService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class CrudContext extends EndPoint<CrudLayerInterface> {

    private static final String QUERY_PATH = "query";
    private static final String QUERY_PARAMETER_PATH = "q";

    private static final Integer CRUD_RESOURCE_NAME_INDEX = 3;
    private static final Integer CRUD_RESOURCE_ACTION_INDEX = 4;
    private static final Integer CRUD_QUERY_ID_INDEX = 5;

    public CrudContext(String groupName, String resourceName) {
        super(groupName, resourceName);
    }

    /**
     * @param request
     * @return
     */
    @Override
    protected LayeredRequest decode(HttpRequest request) {
        if(request.getPathParts().size() <= CRUD_RESOURCE_NAME_INDEX) {
            throw new IllegalArgumentException("Resource name parameter not found");
        }

        String resourceName = request.getPathParts().get(CRUD_RESOURCE_NAME_INDEX);
        String resourceAction = null;
        String id = null;
        if(request.getPathParts().size() > CRUD_RESOURCE_ACTION_INDEX) {
            resourceAction = request.getPathParts().get(CRUD_RESOURCE_ACTION_INDEX);
            if(resourceAction.equals(QUERY_PATH)) {
                //In this case the action is over the resource's query
                if(request.getPathParts().size() > CRUD_QUERY_ID_INDEX) {
                    id = request.getPathParts().get(CRUD_QUERY_ID_INDEX);
                }
            } else if(resourceAction.equals(QUERY_PARAMETER_PATH)){
                //In this case the action is over the resource object.
                if(request.getPathParts().size() > CRUD_QUERY_ID_INDEX) {
                    id = request.getPathParts().get(CRUD_QUERY_ID_INDEX);
                } else {
                    throw new IllegalArgumentException("Resource query parameter not found.");
                }
            } else {
                //In this case the 3 path is the id of the resource.
                id = resourceAction;
                resourceAction = null;
            }
        }

        Class resourceType = getLayerInterface(resourceName).getResourceType();
        Object object = null;
        Map<String, Object> parameters = new HashMap<>();
        DecodedPackage decodedPackage = decode(request, resourceType);
        object = decodedPackage.getObject();
        parameters.putAll(decodedPackage.getParameters());
        parameters.putAll(request.getParameters());

        LayeredRequest result = new LayeredRequest(request,
                parameters, object, resourceName, resourceAction, id);
        return result;
    }

    /**
     *
     * @param request
     * @param resourceType
     * @return
     */
    private DecodedPackage decode(HttpRequest request, Class resourceType) {
        HttpHeader contentTypeHeader = request.getHeader(HttpHeader.CONTENT_TYPE);
        String implName = contentTypeHeader.getParameter(
                contentTypeHeader.getGroups().iterator().next(), HttpHeader.PARAM_IMPL);
        MimeType type = MimeType.fromString(contentTypeHeader.getGroups().iterator().next());
        Map<String, Object> parameters = new HashMap<>();
        parameters.putAll(request.getParameters());
        return EncodingService.decode(type, implName, resourceType, request.getBody(), parameters);
    }

    /**
     * @param object
     * @param request
     * @return
     */
    @Override
    protected LayeredResponse encode(Object object, LayeredRequest request) {
        return null;
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    @Override
    protected final Object post(LayeredRequest layeredRequest) {
        Object result = null;
        CrudLayerInterface layerInterface = getLayerInterface(layeredRequest.getResourceName());
        if(layeredRequest.getResourceAction() == null) {
            result = layerInterface.create(layeredRequest.getAttach(), layeredRequest.getRestParameters());
        } else if(layeredRequest.getResourceAction().equals(QUERY_PARAMETER_PATH)) {
            throw new IllegalArgumentException("The resources can't be created using a query like a parameter.");
        } else if(layeredRequest.getResourceAction().equals(QUERY_PATH)) {
            result = layerInterface.createQuery((Query) layeredRequest.getAttach(), layeredRequest.getRestParameters());
        }
        return result;
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    @Override
    protected final Object get(LayeredRequest layeredRequest) {
        Object result = null;
        CrudLayerInterface layerInterface = getLayerInterface(layeredRequest.getResourceName());
        if(layeredRequest.getResourceAction() == null) {
            result = layerInterface.read(layeredRequest.getId());
        } else if(layeredRequest.getResourceAction().equals(QUERY_PARAMETER_PATH)) {
            result = layerInterface.read(UUID.fromString(layeredRequest.getId()));
        } else if(layeredRequest.getResourceAction().equals(QUERY_PATH)) {
            result = layerInterface.readQuery(new Query.QueryId(UUID.fromString(layeredRequest.getId())));
        }
        return result;
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    @Override
    protected final Object put(LayeredRequest layeredRequest) {
        Object result = null;
        CrudLayerInterface layerInterface = getLayerInterface(layeredRequest.getResourceName());
        if(layeredRequest.getResourceAction() == null) {
            result = layerInterface.update(layeredRequest.getAttach(), layeredRequest.getRestParameters());
        } else if(layeredRequest.getResourceAction().equals(QUERY_PARAMETER_PATH)) {
            result = layerInterface.update(new Query.QueryId(UUID.fromString(layeredRequest.getId())), layeredRequest.getRestParameters());
        } else if(layeredRequest.getResourceAction().equals(QUERY_PATH)) {
            result = layerInterface.updateQuery((Query) layeredRequest.getAttach(), layeredRequest.getRestParameters());
        }
        return result;
    }

    /**
     *
     * @param layeredRequest
     * @return
     */
    @Override
    protected final Object delete(LayeredRequest layeredRequest) {
        Object result = null;
        CrudLayerInterface layerInterface = getLayerInterface(layeredRequest.getResourceName());
        if(layeredRequest.getResourceAction() == null) {
            result = layerInterface.delete(layeredRequest.getId());
        } else if(layeredRequest.getResourceAction().equals(QUERY_PARAMETER_PATH)) {
            result = layerInterface.delete(new Query.QueryId(UUID.fromString(layeredRequest.getId())));
        } else if(layeredRequest.getResourceAction().equals(QUERY_PATH)) {
            result = layerInterface.deleteQuery(new Query.QueryId(UUID.fromString(layeredRequest.getId())));
        }
        return result;
    }
}
