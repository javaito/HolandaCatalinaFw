package org.hcjf.io.net.http.rest.layers;

import com.google.gson.Gson;
import org.hcjf.encoding.MimeType;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.rest.EndPointCrudRequest;
import org.hcjf.io.net.http.rest.EndPointRequest;
import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.layers.query.Query;
import org.hcjf.properties.SystemProperties;

/**
 * This layer interface provides the statement to decode a http request and
 * create a end point package.
 * @author javaito
 */
public interface EndPointDecoderLayerInterface extends LayerInterface {

    /**
     * The implementation of this method must create a end point package
     * using the http request information.
     * @param request Http request.
     * @return End point package.
     */
    public EndPointRequest decode(HttpRequest request, CrudLayerInterface layer);

    /**
     * This is the default decode implementation for JSON
     */
    public static class JsonEndPointDecoder extends Layer implements EndPointDecoderLayerInterface {

        private final Gson gson;

        public JsonEndPointDecoder() {
            super(MimeType.APPLICATION_JSON.toString());
            gson = new Gson();
        }

        /**
         * Decode de request to create a crud invocation from the request data.
         * @param request Http request.
         * @param layer Crud layer to call.
         * @return End point request.
         */
        @Override
        public EndPointRequest decode(HttpRequest request, CrudLayerInterface layer) {
            EndPointRequest result = null;
            switch (request.getMethod()) {
                case POST: {
                    Object bodyObject = gson.fromJson(new String(request.getBody()), layer.getResourceType());
                    result = new EndPointCrudRequest(request, layer,
                            (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(CrudLayerInterface.CrudMethodStatement.CREATE_OBJECT.toString()),
                            bodyObject);
                    break;
                }
                case GET: {
                    if(request.getParameters().containsKey(SystemProperties.get(SystemProperties.Net.Rest.QUERY_PARAMETER))) {
                        Query query = Query.compile(request.getParameter(SystemProperties.get(SystemProperties.Net.Rest.QUERY_PARAMETER)));
                        result = new EndPointCrudRequest(request, layer,
                                (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(CrudLayerInterface.CrudMethodStatement.READ_QUERY.toString()),
                                query);
                    } else {
                        String id = request.getPathParts().get(request.getPathParts().size() - 1);
                        result = new EndPointCrudRequest(request, layer,
                                (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(CrudLayerInterface.CrudMethodStatement.READ_ID.toString()),
                                id);
                    }
                    break;
                }
                case PUT: {
                    Object bodyObject = gson.fromJson(new String(request.getBody()), layer.getResourceType());
                    result = new EndPointCrudRequest(request, layer,
                            (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(CrudLayerInterface.CrudMethodStatement.UPDATE_OBJECT.toString()),
                            bodyObject);
                    break;
                }
                case DELETE: {
                    String id = request.getPathParts().get(request.getPathParts().size() - 1);
                    result = new EndPointCrudRequest(request, layer,
                            (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(CrudLayerInterface.CrudMethodStatement.DELETE_ID.toString()),
                            id);
                    break;
                }
            }

            if(result == null) {
                throw new NullPointerException("Null end point result");
            }

            return result;
        }
    }
}
