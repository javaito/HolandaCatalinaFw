package org.hcjf.io.net.http;

import com.google.gson.*;
import org.hcjf.encoding.MimeType;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.CreateLayerInterface;
import org.hcjf.layers.crud.DeleteLayerInterface;
import org.hcjf.layers.crud.ReadLayerInterface;
import org.hcjf.layers.crud.UpdateLayerInterface;
import org.hcjf.layers.query.*;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.JsonUtils;
import org.hcjf.utils.Strings;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class provides an abstraction to implements a simple rest end point that
 * join any rest rule with some layer implementation.
 * @author javaito
 */
public class RestContext extends Context {

    private static class Fields {
        private static final String VALUE_FIELD = "value";
        private static final String PARAMS_FIELD = "params";
        private static final String ID_URL_FIELD = "id";
        private static final String REQUEST_CONFIG = "__request_config";
        private static final String DATE_FORMAT_CONFIG = "dateFormat";
        private static class Throwable {
            private static final String MESSAGE = "message";
            private static final String EXCEPTION = "exception";
            private static final String BODY = "body";
            private static final String TAGS = "tags";
        }
    }

    private static final String REGEX_TEMPLATE = "\\/%s(\\/(?<resource>[A-Za-z0-9\\-\\_]{0,})){0,}";
    private static final String DEFAULT_QUERY_PARAMETER = "q";

    private final List<Pattern> idRegexList;

    public RestContext(String baseContext) {
        this(baseContext, List.of(SystemProperties.getPattern(SystemProperties.HCJF_UUID_REGEX)));
    }

    public RestContext(String baseContext, List<Pattern> idRegexList) {
        super(String.format(REGEX_TEMPLATE, Strings.trim(baseContext, Strings.SLASH)));
        this.idRegexList = idRegexList;
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
        Gson gson = new GsonBuilder().setDateFormat(SystemProperties.get(SystemProperties.HCJF_DEFAULT_DATE_FORMAT)).create();
        JsonElement jsonElement;
        Collection<HttpHeader> headers = new ArrayList<>();

        String lastPart = request.getPathParts().get(request.getPathParts().size() -1);
        Object id = null;
        for(Pattern idRegex : idRegexList) {
            if(idRegex.matcher(lastPart).matches()) {
                id = Strings.deductInstance(lastPart);
                break;
            }
        }
        String resourceName = Strings.join(request.getPathParts().stream().skip(1).limit(request.getPathParts().size() -
                (id == null ? 0 : 2)), Strings.CLASS_SEPARATOR);

        if(method.equals(HttpMethod.GET)) {
            if(id == null) {
                if (request.hasParameter(DEFAULT_QUERY_PARAMETER)) {
                    Queryable queryable = Query.compile(request.getParameter(DEFAULT_QUERY_PARAMETER));
                    Collection<JoinableMap> queryResult = Query.evaluate(queryable);
                    if(queryResult instanceof ResultSet) {
                        ResultSet<JoinableMap> resultSet = (ResultSet<JoinableMap>) queryResult;
                        headers.add(new HttpHeader(HttpHeader.X_HCJF_QUERY_TOTAL_TIME, resultSet.getTotalTime().toString()));
                        headers.add(new HttpHeader(HttpHeader.X_HCJF_QUERY_TIME_COMPILING, resultSet.getTimeCompilingQuery().toString()));
                        headers.add(new HttpHeader(HttpHeader.X_HCJF_QUERY_TIME_COLLECTING_DATA, resultSet.getTimeCollectingData().toString()));
                        headers.add(new HttpHeader(HttpHeader.X_HCJF_QUERY_TIME_EVALUATING_CONDITIONS, resultSet.getTimeEvaluatingConditions().toString()));
                        headers.add(new HttpHeader(HttpHeader.X_HCJF_QUERY_AVERAGE_TIME_EVALUATING_CONDITIONS, resultSet.getAverageTimeFormattingDataByRow().toString()));
                        headers.add(new HttpHeader(HttpHeader.X_HCJF_QUERY_TIME_FORMATTING_DATA, resultSet.getTimeFormattingData().toString()));
                        headers.add(new HttpHeader(HttpHeader.X_HCJF_QUERY_AVERAGE_TIME_FORMATTING_DATA, resultSet.getAverageTimeFormattingDataByRow().toString()));
                        headers.add(new HttpHeader(HttpHeader.X_HCJF_QUERY_PRESENT_FIELDS, Strings.join(resultSet.getPresentFields(), Strings.ARGUMENT_SEPARATOR)));
                    }
                    jsonElement = gson.toJsonTree(queryResult);
                } else {
                    ReadLayerInterface readLayerInterface = Layers.get(ReadLayerInterface.class, resourceName);
                    jsonElement = gson.toJsonTree(readLayerInterface.read());
                }
            } else {
                ReadLayerInterface readLayerInterface = Layers.get(ReadLayerInterface.class, resourceName);
                jsonElement = gson.toJsonTree(readLayerInterface.read(id));
            }
        } else if(method.equals(HttpMethod.POST)) {
            RequestModel requestModel = new RequestModel((JsonObject) jsonParser.parse(new String(request.getBody())));
            if(requestModel.getBody() == null) {
                // If the method is post and the body object is null then the request attempt to create a parameterized
                // query instance or a group of queryable instances.
                if(requestModel.getQueryable() != null) {
                    jsonElement = gson.toJsonTree(Query.evaluate(requestModel.getQueryable()));
                } else if(requestModel.getQueryables() != null){
                    JsonObject queriesResult = new JsonObject();
                    for(String key : requestModel.getQueryables().keySet()) {
                        try {
                            queriesResult.add(key, gson.toJsonTree(Query.evaluate(requestModel.getQueryables().get(key))));
                        } catch (Throwable throwable){
                            queriesResult.add(key, createJsonFromThrowable(throwable));
                        }
                    }
                    jsonElement = queriesResult;
                } else {
                    throw new HCJFRuntimeException("Unsupported POST method configuration");
                }
            } else {
                // This method call by default to create layer interface implementation.
                CreateLayerInterface createLayerInterface = Layers.get(CreateLayerInterface.class, resourceName);
                jsonElement = gson.toJsonTree(createLayerInterface.create(requestModel.getBody()));
            }
        } else if(method.equals(HttpMethod.PUT)) {
            // This method call to update layer interface implementation.
            UpdateLayerInterface updateLayerInterface = Layers.get(UpdateLayerInterface.class, resourceName);
            RequestModel requestModel = new RequestModel((JsonObject) jsonParser.parse(new String(request.getBody())));
            if(id != null) {
                requestModel.getBody().put(Fields.ID_URL_FIELD, id);
                jsonElement = gson.toJsonTree(updateLayerInterface.update(requestModel.getBody()));
            } else {
                jsonElement = gson.toJsonTree(updateLayerInterface.update(requestModel.queryable, requestModel.getBody()));
            }
        } else if(method.equals(HttpMethod.DELETE)) {
            // This method call to delete layer interface implementation.
            DeleteLayerInterface deleteLayerInterface = Layers.get(DeleteLayerInterface.class, resourceName);
            if(id != null) {
                jsonElement = gson.toJsonTree(deleteLayerInterface.delete(id));
            } else {
                RequestModel requestModel = new RequestModel((JsonObject) jsonParser.parse(new String(request.getBody())));
                jsonElement = gson.toJsonTree(deleteLayerInterface.delete(requestModel.getQueryable()));
            }
        } else {
            throw new HCJFRuntimeException("Unsupported http method: %s", method.toString());
        }

        HttpResponse response = new HttpResponse();
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
        byte[] body = jsonElement.toString().getBytes();
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
        for(HttpHeader header : headers) {
            response.addHeader(header);
        }
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
        Map<String,String> tags = getTags(throwable, new HashMap<>());
        if(!tags.isEmpty()) {
            JsonObject jsonTags = new JsonObject();
            for(String tag : tags.keySet()) {
                jsonTags.addProperty(tag, tags.get(tag));
            }
            jsonObject.add(Fields.Throwable.TAGS, jsonTags);
        }
        jsonObject.addProperty(Fields.Throwable.EXCEPTION, throwable.getClass().getName());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        throwable.printStackTrace(printStream);
        jsonObject.addProperty(Fields.Throwable.BODY, byteArrayOutputStream.toString());
        return jsonObject;
    }

    /**
     * Find all the tags into the throwable instances and his causes.
     * @param throwable Throwable instance.
     * @param tag Map to store the tags founded.
     * @return Return the same map sending as parameter.
     */
    private Map<String,String> getTags(Throwable throwable, Map<String,String> tag) {
        if(throwable.getMessage() != null) {
            tag.putAll(Strings.getTagsFromMessage(throwable.getMessage()));
        }
        if(throwable.getCause() != null) {
            tag = getTags(throwable.getCause(), tag);
        }
        return tag;
    }

    /**
     * This inner class contains the necessary methods to parse the request body in order to call
     * the specific layer implementation.
     */
    private static class RequestModel {

        private Map<String,Object> body;
        private Queryable queryable;
        private Map<String,Queryable> queryables;
        private Map<String,Object> requestConfig;

        public RequestModel(JsonObject jsonObject) {
            if(!jsonObject.has(SystemProperties.get(SystemProperties.Net.Rest.BODY_FIELD)) &&
                    !jsonObject.has(SystemProperties.get(SystemProperties.Net.Rest.QUERY_FIELD)) &&
                    !jsonObject.has(SystemProperties.get(SystemProperties.Net.Rest.QUERIES_FIELD))) {
                body = JsonUtils.createBody(jsonObject);
            } else {
                if (jsonObject.has(SystemProperties.get(SystemProperties.Net.Rest.BODY_FIELD))) {
                    body = JsonUtils.createBody(jsonObject.getAsJsonObject(SystemProperties.get(SystemProperties.Net.Rest.BODY_FIELD)));
                }

                if (jsonObject.has(SystemProperties.get(SystemProperties.Net.Rest.QUERY_FIELD))) {
                    queryable = createQuery(jsonObject.get(SystemProperties.get(SystemProperties.Net.Rest.QUERY_FIELD)));
                }

                if (jsonObject.has(SystemProperties.get(SystemProperties.Net.Rest.QUERIES_FIELD))) {
                    queryables = new HashMap<>();
                    JsonObject queryablesObject = jsonObject.getAsJsonObject(SystemProperties.get(SystemProperties.Net.Rest.QUERIES_FIELD));
                    for (String key : queryablesObject.keySet()) {
                        queryables.put(key, createQuery(queryablesObject.get(key)));
                    }
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
                    for (Object parameter : JsonUtils.createList(queryJsonObject.get(Fields.PARAMS_FIELD).getAsJsonArray())) {
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
        public Map<String, Queryable> getQueryables() {
            return queryables;
        }
    }
}
