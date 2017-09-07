package org.hcjf.io.net.http.rest.layers;

import com.google.gson.*;
import org.hcjf.encoding.MimeType;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.rest.EndPointCrudRequest;
import org.hcjf.io.net.http.rest.EndPointRequest;
import org.hcjf.io.net.http.rest.References;
import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.layers.query.Query;
import org.hcjf.properties.SystemProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     * @param layer Crud layer instance.
     * @return End point package.
     */
    public EndPointRequest decode(HttpRequest request, CrudLayerInterface layer);

    /**
     * This is the default decode implementation for JSON
     */
    public static class JsonEndPointDecoder extends Layer implements EndPointDecoderLayerInterface, ExclusionStrategy {

        private final Gson gson;
        private final JsonParser jsonParser;

        public JsonEndPointDecoder() {
            super(MimeType.APPLICATION_JSON.toString());
            gson = new GsonBuilder().addSerializationExclusionStrategy(this).create();
            jsonParser = new JsonParser();
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
                    JsonObject jsonBody = (JsonObject) jsonParser.parse(new String(request.getBody()));
                    if(jsonBody.has(References.REFERENCES_FIELD_NAME)) {
                        Map<String, Object> parameters = new HashMap<>();
                        References references = new JsonReferences((JsonObject) jsonBody.remove(References.REFERENCES_FIELD_NAME));
                        parameters.put(References.REFERENCES_FIELD_NAME, references);
                        Object bodyObject = gson.fromJson(jsonBody, layer.getResourceType());
                        result = new EndPointCrudRequest(request, layer,
                                (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(
                                        CrudLayerInterface.CrudMethodStatement.CREATE_OBJECT_MAP.toString()), bodyObject, parameters);
                    } else {
                        Object bodyObject = gson.fromJson(jsonBody, layer.getResourceType());
                        result = new EndPointCrudRequest(request, layer,
                                (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(
                                        CrudLayerInterface.CrudMethodStatement.CREATE_OBJECT.toString()), bodyObject);
                    }
                    break;
                }
                case GET: {
                    if(request.getParameters().containsKey(SystemProperties.get(SystemProperties.Net.Rest.QUERY_PARAMETER))) {
                        Query query = Query.compile(request.getParameter(SystemProperties.get(SystemProperties.Net.Rest.QUERY_PARAMETER)));
                        result = new EndPointCrudRequest(request, layer,
                                (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(
                                        CrudLayerInterface.CrudMethodStatement.READ_QUERY.toString()), query);
                    } else {
                        String id = request.getPathParts().get(request.getPathParts().size() - 1);
                        result = new EndPointCrudRequest(request, layer,
                                (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(
                                        CrudLayerInterface.CrudMethodStatement.READ_ID.toString()), id);
                    }
                    break;
                }
                case PUT: {
                    JsonObject jsonBody = (JsonObject) jsonParser.parse(new String(request.getBody()));
                    if(jsonBody.has(References.REFERENCES_FIELD_NAME)) {
                        Map<String, Object> parameters = new HashMap<>();
                        References references = new JsonReferences((JsonObject) jsonBody.remove(References.REFERENCES_FIELD_NAME));
                        parameters.put(References.REFERENCES_FIELD_NAME, references);
                        Object bodyObject = gson.fromJson(jsonBody, layer.getResourceType());
                        result = new EndPointCrudRequest(request, layer,
                                (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(
                                        CrudLayerInterface.CrudMethodStatement.UPDATE_OBJECT_MAP.toString()), bodyObject, parameters);
                    } else {
                        Object bodyObject = gson.fromJson(jsonBody, layer.getResourceType());
                        result = new EndPointCrudRequest(request, layer,
                                (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(
                                        CrudLayerInterface.CrudMethodStatement.UPDATE_OBJECT.toString()), bodyObject);
                    }
                    break;
                }
                case DELETE: {
                    String id = request.getPathParts().get(request.getPathParts().size() - 1);
                    result = new EndPointCrudRequest(request, layer,
                            (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(
                                    CrudLayerInterface.CrudMethodStatement.DELETE_ID.toString()), id);
                    break;
                }
            }

            if(result == null) {
                throw new NullPointerException("Null end point result");
            }

            return result;
        }

        /**
         * This method verify if the attribute must by skipped for the
         * json decoding process.
         * @param fieldAttributes Field attribute.
         * @return True if the field must be excluded and false in the otherwise.
         */
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return false;
        }

        /**
         * This method verify if the class must be excluded.
         * @param aClass Evaluation class.
         * @return Ture if the class must be excluded and false in the otherwise.
         */
        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    }

    public static class JsonReferences implements References {

        private static final String RESOURCE_FIELD = "__resource__";

        private final JsonObject jsonObject;
        private final Gson gson;

        public JsonReferences(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
            gson = new Gson();
        }

        private Object getReferenceInstance(JsonObject jsonObject) {
            CrudLayerInterface crudLayerInterface = Layers.get(CrudLayerInterface.class,
                    jsonObject.get(RESOURCE_FIELD).getAsString());
            return gson.fromJson(jsonObject, crudLayerInterface.getResourceType());
        }

        @Override
        public <O> O getReference(String referenceName) {
            O result = null;
            if(jsonObject.has(referenceName)) {
                JsonElement jsonElement = jsonObject.get(referenceName);
                if (jsonElement instanceof JsonObject) {
                    result = (O) getReferenceInstance((JsonObject) jsonElement);
                }
            }
            return result;
        }

        public <O> Collection<O> getReferenceCollection(String referenceName) {
            Collection<O> result = new ArrayList<>();
            if(jsonObject.has(referenceName)) {
                JsonElement jsonElement = jsonObject.get(referenceName);
                if (jsonElement instanceof JsonArray) {
                    for (JsonElement arrayElement : ((JsonArray) jsonElement)) {
                        result.add((O) getReferenceInstance((JsonObject) arrayElement));
                    }
                }
            }
            return result;
        }
    }
}
