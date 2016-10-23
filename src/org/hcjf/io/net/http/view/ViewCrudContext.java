package org.hcjf.io.net.http.view;

import org.hcjf.encoding.DecodedPackage;
import org.hcjf.encoding.EncodingService;
import org.hcjf.encoding.MimeType;
import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.HttpResponseCode;
import org.hcjf.layers.view.ViewCrudLayerInterface;
import org.hcjf.view.components.ViewDataSet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class ViewCrudContext extends  ViewContext<ViewCrudLayerInterface, ViewRequest, ViewResponse>{

    private static final Integer ENCODING_IMPLEMENTATION_INDEX = 3;
    private static final Integer RESOURCE_NAME_INDEX = 4;
    private static final Integer RESOURCE_ACTION_INDEX = 5;

    public ViewCrudContext(String groupName, String resourceName) {
        super(groupName, resourceName);
    }

    @Override
    protected ViewRequest decode(HttpRequest request) {

        if(request.getPathParts().size() <= RESOURCE_NAME_INDEX) {
            throw new IllegalArgumentException("Resource name parameter not found");
        }

        String resourceName = request.getPathParts().get(RESOURCE_NAME_INDEX);
        String resourceAction = "list"; // Default action
        String encodingImplementation = request.getPathParts().get(ENCODING_IMPLEMENTATION_INDEX);

        if(request.getPathParts().size()-1 >= RESOURCE_ACTION_INDEX) {
            resourceAction = request.getPathParts().get(RESOURCE_ACTION_INDEX);
            //Only 2 actions permitted in view crud context
            if(!resourceAction.equals("list") &&
                    !resourceAction.equals("crud") &&
                    !resourceAction.equals("data")){
                throw new IllegalArgumentException("Resource action forbidden");
            }
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.putAll(request.getParameters());

        ViewRequest result = new ViewRequest(request,resourceAction,resourceName, encodingImplementation, parameters);

        return result;
    }

    @Override
    protected ViewResponse encode(Object object, ViewRequest request) {

        byte[] body = EncodingService.encode(MimeType.TEXT_HTML, request.getEncodingImplementation(), new DecodedPackage(object,new HashMap<String, Object>()));

        MimeType mimeType = object instanceof ViewDataSet ? MimeType.APPLICATION_JSON : MimeType.TEXT_HTML;
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.OK);
        response.setReasonPhrase("VIEW Success");
        response.setBody(body);
        response.addHeader(new HttpHeader("Cache-Control","private, max-age=86400"));
        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, mimeType.toString()));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
        return new ViewResponse(response);
    }
}
