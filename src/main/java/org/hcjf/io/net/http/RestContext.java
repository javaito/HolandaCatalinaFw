package org.hcjf.io.net.http;

import com.google.gson.*;
import org.hcjf.encoding.MimeType;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.CreateLayerInterface;
import org.hcjf.layers.crud.DeleteLayerInterface;
import org.hcjf.layers.crud.UpdateLayerInterface;
import org.hcjf.layers.query.ParameterizedQuery;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.Queryable;
import org.hcjf.utils.Strings;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides an abstraction to implements a simple rest end point that
 * join any rest rule with some layer implementation.
 * @author javaito
 */
public class RestContext extends Context {

    private static class Fields {
        private static final String BODY_FIELD = "body";
        private static final String QUERY_FIELD = "query";
        private static final String VALUE_FIELD = "value";
        private static final String PARAMS_FIELD = "params";
        private static final String QUERIES_FIELD = "queries";
        private static final String ID_URL_FIELD = "id";
        private static class Throwable {
            private static final String MESSAGE = "message";
            private static final String EXCEPTION = "exception";
            private static final String BODY = "body";
        }
    }

    private static final String REGEX_TEMPLATE = "\\/%s(\\/(?<id>[A-Za-z0-9\\-]{0,}))?(\\/.*)?(\\?.*)?";
    private static final String DEFAULT_QUERY_PARAMETER = "q";

    public RestContext(String baseContext) {
        super(String.format(REGEX_TEMPLATE, Strings.trim(baseContext, Strings.SLASH)));
    }

    /**
     * This method receive the http request and delegate each request for the different method depends of the
     * http method and the request body.
     * @param request All the request information.
     * @return Returns the response object.
     */
    @Override
    public HttpResponse onContext(HttpRequest request) {
        HttpMethod method = request.getMethod();
        JsonParser jsonParser = new JsonParser();
        Gson gson = new GsonBuilder().create();
        JsonElement jsonElement;
        String resourceName = request.getPathParts().get(request.getPathParts().size() - 1);

        if(method.equals(HttpMethod.GET)) {
            //If the method is get then compile the query into the 'q' parameter.
            String id = request.getReplaceableValue(Fields.ID_URL_FIELD);
            if(request.hasParameter(DEFAULT_QUERY_PARAMETER)) {
                Queryable queryable = Query.compile(request.getParameter(DEFAULT_QUERY_PARAMETER));
                jsonElement = gson.toJsonTree(Query.evaluate(queryable));
            } else {
                throw new UnsupportedOperationException("Expected http parameter 'q' or id context");
            }
        } else if(method.equals(HttpMethod.POST)) {
            RequestModel requestModel = new RequestModel((JsonObject) jsonParser.parse(new String(request.getBody())));
            if(requestModel.getBody() == null) {
                // If the method is post and the body object is null then the request attempt to create a parameterized
                // query instance or a group of queriable instances.
                if(requestModel.getQueryable() != null) {
                    jsonElement = gson.toJsonTree(Query.evaluate(requestModel.getQueryable()));
                } else if(requestModel.getQueriables() != null){
                    JsonObject queriesResult = new JsonObject();
                    for(String key : requestModel.getQueriables().keySet()) {
                        try {
                            queriesResult.add(key, gson.toJsonTree(Query.evaluate(requestModel.getQueriables().get(key))));
                        } catch (Throwable throwable){
                            queriesResult.add(key, createJsonFromThrowable(throwable));
                        }
                    }
                    jsonElement = queriesResult;
                } else {
                    throw new UnsupportedOperationException("Unsupported http method: " + method.toString());
                }
            } else {
                // This method call by default to create layer interface implementation.
                CreateLayerInterface createLayerInterface = Layers.get(CreateLayerInterface.class, resourceName);
                jsonElement = gson.toJsonTree(createLayerInterface.create(requestModel.getBody()));
            }
        } else if(method.equals(HttpMethod.PUT)) {
            // This method call to update layer interface implementation.
            RequestModel requestModel = new RequestModel((JsonObject) jsonParser.parse(new String(request.getBody())));
            UpdateLayerInterface updateLayerInterface = Layers.get(UpdateLayerInterface.class, resourceName);
            jsonElement = gson.toJsonTree(updateLayerInterface.update(requestModel.queryable, requestModel.getBody()));
        } else if(method.equals(HttpMethod.DELETE)) {
            // This method call to delete layer interface implementation.
            RequestModel requestModel = new RequestModel((JsonObject) jsonParser.parse(new String(request.getBody())));
            DeleteLayerInterface deleteLayerInterface = Layers.get(DeleteLayerInterface.class, resourceName);
            jsonElement = gson.toJsonTree(deleteLayerInterface.delete(requestModel.getQueryable()));
        } else {
            throw new UnsupportedOperationException("Unsupported http method: " + method.toString());
        }

        HttpResponse response = new HttpResponse();
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
        byte[] body = jsonElement.toString().getBytes();
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
        response.setBody(body);

        return response;
    }

    /**
     * This method generate error response all the times that the request generates an throwable instance.
     * @param request All the request information.
     * @param throwable Throwable object, could be null.
     * @return Returns http response instance.
     */
    @Override
    protected HttpResponse onError(HttpRequest request, Throwable throwable) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.BAD_REQUEST);
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
        byte[] body = createJsonFromThrowable(throwable).toString().getBytes();
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
        response.setBody(body);

        return response;
    }

    /**
     * This method create a json object from a throwable.
     * @param throwable throwable instance.
     * @return Json object.
     */
    private JsonObject createJsonFromThrowable(Throwable throwable) {
        JsonObject jsonObject = new JsonObject();
        if(throwable.getMessage() != null) {
            jsonObject.addProperty(Fields.Throwable.MESSAGE, throwable.getMessage());
        }
        jsonObject.addProperty(Fields.Throwable.EXCEPTION, throwable.getClass().getName());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        throwable.printStackTrace(printStream);
        jsonObject.addProperty(Fields.Throwable.BODY, byteArrayOutputStream.toString());
        return jsonObject;
    }

    /**
     * This inner class contains the necessary methods to parse the request body in order to call
     * the specific layer implementation.
     */
    private static class RequestModel {

        private Map<String,Object> body;
        private Queryable queryable;
        private Map<String,Queryable> queriables;

        public RequestModel(JsonObject jsonObject) {
            if(jsonObject.has(Fields.BODY_FIELD)) {
                body = createBody(jsonObject.getAsJsonObject(Fields.BODY_FIELD));
            }

            if(jsonObject.has(Fields.QUERY_FIELD)) {
                queryable = createQuery(jsonObject.get(Fields.QUERY_FIELD));
            }

            if(jsonObject.has(Fields.QUERIES_FIELD)) {
                queriables = new HashMap<>();
                JsonObject queryablesObject = jsonObject.getAsJsonObject(Fields.QUERIES_FIELD);
                for(String key : queryablesObject.keySet()) {
                    queriables.put(key, createQuery(queryablesObject.get(key)));
                }
            }
        }

        /**
         * Creates the queryable instance from a json element.
         * @param element Json element instance.
         * @return Returns the queriable instance.
         */
        private Queryable createQuery(JsonElement element) {
            Queryable result;
            if(element instanceof JsonObject) {
                JsonObject queryJsonObject = element.getAsJsonObject();
                ParameterizedQuery parameterizedQuery = Query.compile(queryJsonObject.get(Fields.VALUE_FIELD).getAsString()).getParameterizedQuery();
                if(queryJsonObject.has(Fields.PARAMS_FIELD)) {
                    for (Object parameter : createList(queryJsonObject.get(Fields.PARAMS_FIELD).getAsJsonArray())) {
                        parameterizedQuery.add(parameter);
                    }
                }
                result = parameterizedQuery;
            } else {
                result = Query.compile(element.getAsString());
            }
            return result;
        }

        /**
         * Creates the body object from a json object.
         * @param jsonObject Json object instance.
         * @return Map with all the fields of the object.
         */
        private Map<String,Object> createBody(JsonObject jsonObject) {
            Map<String,Object> result = new HashMap<>();
            for(String fieldName : jsonObject.keySet()) {
                result.put(fieldName, createObject(jsonObject.get(fieldName)));
            }
            return result;
        }

        /**
         * Creates the list instance from a json array.
         * @param array Json array instance.
         * @return List instance.
         */
        private List<Object> createList(JsonArray array) {
            List<Object> result = new ArrayList<>();
            for(JsonElement currentElement : array) {
                result.add(createObject(currentElement));
            }
            return result;
        }

        /**
         * Creates the object instance from a json element.
         * @param element Json element.
         * @return Object instance.
         */
        private Object createObject(JsonElement element) {
            Object value;
            if(element instanceof JsonObject) {
                value = createBody((JsonObject) element);
            } else if(element instanceof JsonArray) {
                value = createList((JsonArray) element);
            } else {
                value = Strings.deductInstance(element.getAsString());
            }
            return value;
        }

        /**
         * Returns the body of the request.
         * @return Body of the request.
         */
        public Map<String, Object> getBody() {
            return body;
        }

        /**
         * Returns the queryable instance of the request.
         * @return Queriable instace.
         */
        public Queryable getQueryable() {
            return queryable;
        }

        /**
         * Returns a map of the queryable instances of the request.
         * @return Queryable instances of the request.
         */
        public Map<String, Queryable> getQueriables() {
            return queriables;
        }
    }
}
