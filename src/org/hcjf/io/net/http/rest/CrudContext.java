package org.hcjf.io.net.http.rest;

import org.hcjf.encoding.CrudDecodedPackage;
import org.hcjf.io.net.http.*;
import org.hcjf.io.net.http.layered.LayeredRequest;
import org.hcjf.io.net.http.layered.LayeredResponse;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.encoding.DecodedPackage;
import org.hcjf.encoding.MimeType;
import org.hcjf.encoding.EncodingService;
import org.hcjf.properties.SystemProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class CrudContext extends EndPoint<CrudLayerInterface, CrudRequest, CrudResponse> {

    private static final Integer CRUD_RESOURCE_NAME_INDEX = 2;
    private static final Integer CRUD_RESOURCE_ACTION_INDEX = 3;
    private static final Integer CRUD_QUERY_ID_INDEX = 4;

    public CrudContext(String groupName, String resourceName) {
        super(groupName, resourceName);
    }

    /**
     * @param request
     * @return
     */
    @Override
    protected CrudRequest decode(HttpRequest request) {
        if(request.getPathParts().size() <= CRUD_RESOURCE_NAME_INDEX) {
            throw new IllegalArgumentException("Resource name parameter not found");
        }

        String resourceName = request.getPathParts().get(CRUD_RESOURCE_NAME_INDEX);
        String resourceAction = null;
        String id = null;
        if(request.getPathParts().size() > CRUD_RESOURCE_ACTION_INDEX) {
            resourceAction = request.getPathParts().get(CRUD_RESOURCE_ACTION_INDEX);
            if(resourceAction.equals(SystemProperties.get(SystemProperties.REST_QUERY_PATH))) {
                //In this case the action is over the resource's query
                if(request.getPathParts().size() > CRUD_QUERY_ID_INDEX) {
                    id = request.getPathParts().get(CRUD_QUERY_ID_INDEX);
                }
            } else if(resourceAction.equals(SystemProperties.get(SystemProperties.REST_QUERY_PARAMETER_PATH))){
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

        CrudRequest result = new CrudRequest(request,
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
        DecodedPackage result;
        if(request.getMethod().equals(HttpMethod.GET) || request.getMethod().equals(HttpMethod.DELETE)) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.putAll(request.getParameters());
            result = new CrudDecodedPackage(null, null, parameters);
        } else {
            HttpHeader contentTypeHeader = request.getHeader(HttpHeader.CONTENT_TYPE);
            String implName = contentTypeHeader.getParameter(
                    contentTypeHeader.getGroups().iterator().next(), HttpHeader.PARAM_IMPL);
            MimeType type = MimeType.fromString(contentTypeHeader.getGroups().iterator().next());
            Map<String, Object> parameters = new HashMap<>();
            parameters.putAll(request.getParameters());
            result = EncodingService.decode(type, implName, resourceType, request.getBody(), parameters);
        }
        return result;
    }

    /**
     * @param object
     * @param request
     * @return
     */
    @Override
    protected CrudResponse encode(Object object, CrudRequest request) {
        HttpHeader contentTypeHeader = request.getHeader(HttpHeader.ACCEPT);
        String implName = contentTypeHeader.getParameter(
                contentTypeHeader.getGroups().iterator().next(), HttpHeader.PARAM_IMPL);
        MimeType type = MimeType.fromString(contentTypeHeader.getGroups().iterator().next());
        byte[] body = EncodingService.encode(type, implName, new CrudDecodedPackage(object, null, new HashMap<String, Object>()));

        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.OK);
        response.setReasonPhrase("REST Success");
        response.setBody(body);
        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, type.toString()));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
        return new CrudResponse(response);
    }

    /**
     *
     * @param crudRequest
     * @return
     */
    @Override
    protected final Object post(CrudRequest crudRequest) {
        Object result = null;
        CrudLayerInterface layerInterface = getLayerInterface(crudRequest.getResourceName());
        if(crudRequest.getResourceAction() == null) {
            result = layerInterface.create(crudRequest.getAttach(), crudRequest.getCrudParameters());
        } else if(crudRequest.getResourceAction().equals(SystemProperties.get(SystemProperties.REST_QUERY_PARAMETER_PATH))) {
            throw new IllegalArgumentException("The resources can't be created using a query like a parameter.");
        } else if(crudRequest.getResourceAction().equals(SystemProperties.get(SystemProperties.REST_QUERY_PATH))) {
            result = layerInterface.createQuery((Query) crudRequest.getAttach(), crudRequest.getCrudParameters());
        }
        return result;
    }

    /**
     *
     * @param crudRequest
     * @return
     */
    @Override
    protected final Object get(CrudRequest crudRequest) {
        Object result = null;
        CrudLayerInterface layerInterface = getLayerInterface(crudRequest.getResourceName());
        if(crudRequest.getResourceAction() == null) {
            if(crudRequest.getId() == null) {
                result = layerInterface.read();
            } else {
                result = layerInterface.read(crudRequest.getId());
            }
        } else if(crudRequest.getResourceAction().equals(SystemProperties.get(SystemProperties.REST_QUERY_PARAMETER_PATH))) {
            result = layerInterface.read(UUID.fromString(crudRequest.getId()));
        } else if(crudRequest.getResourceAction().equals(SystemProperties.get(SystemProperties.REST_QUERY_PATH))) {
            result = layerInterface.readQuery(new Query.QueryId(UUID.fromString(crudRequest.getId())));
        }
        return result;
    }

    /**
     *
     * @param crudRequest
     * @return
     */
    @Override
    protected final Object put(CrudRequest crudRequest) {
        Object result = null;
        CrudLayerInterface layerInterface = getLayerInterface(crudRequest.getResourceName());
        if(crudRequest.getResourceAction() == null) {
            result = layerInterface.update(crudRequest.getAttach(), crudRequest.getCrudParameters());
        } else if(crudRequest.getResourceAction().equals(SystemProperties.get(SystemProperties.REST_QUERY_PARAMETER_PATH))) {
            result = layerInterface.update(new Query.QueryId(UUID.fromString(crudRequest.getId())), crudRequest.getCrudParameters());
        } else if(crudRequest.getResourceAction().equals(SystemProperties.get(SystemProperties.REST_QUERY_PATH))) {
            result = layerInterface.updateQuery((Query) crudRequest.getAttach(), crudRequest.getCrudParameters());
        }
        return result;
    }

    /**
     *
     * @param crudRequest
     * @return
     */
    @Override
    protected final Object delete(CrudRequest crudRequest) {
        Object result = null;
        CrudLayerInterface layerInterface = getLayerInterface(crudRequest.getResourceName());
        if(crudRequest.getResourceAction() == null) {
            result = layerInterface.delete(crudRequest.getId());
        } else if(crudRequest.getResourceAction().equals(SystemProperties.get(SystemProperties.REST_QUERY_PARAMETER_PATH))) {
            result = layerInterface.delete(new Query.QueryId(UUID.fromString(crudRequest.getId())));
        } else if(crudRequest.getResourceAction().equals(SystemProperties.get(SystemProperties.REST_QUERY_PATH))) {
            result = layerInterface.deleteQuery(new Query.QueryId(UUID.fromString(crudRequest.getId())));
        }
        return result;
    }
}
