package org.hcjf.io.net.http.rest.layers;

import com.google.gson.*;
import org.hcjf.encoding.MimeType;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.rest.EndPointCrudRequest;
import org.hcjf.io.net.http.rest.EndPointRequest;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.References;
import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.layers.query.Query;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

        private static final String REFERENCES_PREFIX = "__references_";

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
            Object decodedBody;
            RestReference references;
            Map<String, Object> parameters;
            switch (request.getMethod()) {
                case POST: {
                    parameters = new HashMap<>();
                    references = new RestReference();
                    parameters.put(References.class.getName(), references);
                    decodedBody = createEnityAndReference(jsonParser.parse(new String(request.getBody())), references, layer.getResourceType());
                    result = new EndPointCrudRequest(request, layer,
                            (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(
                                    CrudLayerInterface.CrudMethodStatement.CREATE_OBJECT_MAP.toString()), decodedBody, parameters);
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
                    parameters = new HashMap<>();
                    references = new RestReference();
                    parameters.put(References.class.getName(), references);
                    decodedBody = createEnityAndReference(jsonParser.parse(new String(request.getBody())), references, layer.getResourceType());
                    result = new EndPointCrudRequest(request, layer,
                            (CrudLayerInterface.CrudInvoker) layer.getInvokers().get(
                                    CrudLayerInterface.CrudMethodStatement.UPDATE_OBJECT_MAP.toString()), decodedBody, parameters);
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

        private Object createEnityAndReference(JsonElement element, RestReference defaultReferences, Class resourceType) {
            Object result = null;
            if(element instanceof JsonObject) {
                result = createEnityAndReference((JsonObject)element, defaultReferences, resourceType);
            } else if(element instanceof JsonArray) {
                List resultList = new ArrayList();
                for(JsonElement subElement : (JsonArray)element) {
                    resultList.add(createEnityAndReference(subElement, defaultReferences, resourceType));
                }
                result = resultList;
            }
            return result;
        }

        private Object createEnityAndReference(JsonObject jsonObject, RestReference defaultReferences, Class resourceType) {
            Map<String, Introspection.Setter> setters = Introspection.getSetters(resourceType);
            Object result;
            try {
                result = resourceType.getConstructor().newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to create resource instance " + resourceType.toString(), ex);
            }
            Introspection.Setter setter;
            Object value;
            String referenceName;
            JsonElement element;
            for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if(entry.getKey().startsWith(REFERENCES_PREFIX)) {
                    //This value is a reference object.
                    referenceName = entry.getKey().substring(REFERENCES_PREFIX.length(), entry.getKey().length());
                    element = entry.getValue();
                    setter = setters.get(referenceName);
                    if(setter == null) {
                        continue;
                    }
                    Class referenceType;

                    if(element instanceof JsonObject) {
                        referenceType = inferResourceType(setter, (JsonObject) element);
                        defaultReferences.putReference(referenceName, createEnityAndReference(
                                (JsonObject) element, defaultReferences, referenceType));
                    } else if(element instanceof JsonArray) {
                        try {
                            Collection collection;
                            if(Set.class.isAssignableFrom(setter.getParameterType())) {
                                collection = new HashSet();
                            } else {
                                collection = new ArrayList();
                            }

                            for(JsonElement collectionElemets : ((JsonArray)element)) {
                                referenceType = inferResourceType(setter, (JsonObject) collectionElemets);
                                collection.add(createEnityAndReference(collectionElemets, defaultReferences, referenceType));
                            }
                            defaultReferences.putReference(referenceName, collection);
                        } catch (Exception ex) {
                            Log.w(SystemProperties.Net.Http.LOG_TAG, "Unable to create reference %s", element.toString());
                        }
                    } else if(element instanceof JsonPrimitive) {
                        defaultReferences.putReference(referenceName, element.getAsString());
                    }
                } else {
                    setter = setters.get(entry.getKey());
                    if(setter != null) {
                        value = decodeElement(entry.getValue(), setter);
                        if(value != null) {
                            try {
                                setter.set(result, value);
                            } catch (Exception e) {
                                Log.w(SystemProperties.Net.Http.LOG_TAG, "Unable to set value %s for resource type %s",
                                        value.toString(), resourceType.toString());
                            }
                        }
                    }
                }
            }
            return result;
        }

        protected Class inferResourceType(Introspection.Setter setter, JsonObject jsonObject) {
            Class result;
            if(Collection.class.isAssignableFrom(setter.getParameterType())) {
                result = setter.getParameterCollectionType();
            } else {
                result = setter.getParameterType();
            }
            return result;
        }

        protected Object decodeElement(JsonElement jsonElement, Introspection.Setter setter) {
            Object result = null;
            Class parameterType = setter.getParameterType();
            try {
                if (Map.class.isAssignableFrom(parameterType)) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    Map resultMap = new HashMap();
                    for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        resultMap.put(entry.getKey(), decodeElement(entry.getValue(), setter.getParameterCollectionType()));
                    }
                } else if(Collection.class.isAssignableFrom(parameterType)) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    Collection collectionResult;
                    if(Set.class.isAssignableFrom(parameterType)) {
                        collectionResult = new HashSet();
                    } else {
                        collectionResult = new ArrayList();
                    }
                    for(JsonElement arrayElement : jsonArray) {
                        collectionResult.add(decodeElement(arrayElement, setter.getParameterCollectionType()));
                    }
                } else {
                    result = decodeElement(jsonElement, parameterType);
                }
            } catch (Exception ex){
                Log.w(SystemProperties.Net.Http.LOG_TAG, "Unable to encode %s:%s to %s data type",
                        setter.getResourceName(), jsonElement.toString(), setter.getParameterType().toString());
            }
            return result;
        }

        protected Object decodeElement(JsonElement jsonElement, Class parameterType) {
            Object result = null;
            try {
                if (parameterType.equals(String.class)) {
                    result = jsonElement.getAsString();
                } else if (parameterType.equals(Date.class)) {
                    result = new Date(jsonElement.getAsLong());
                } else if (parameterType.isEnum()) {
                    result = Enum.valueOf(parameterType, jsonElement.getAsString());
                } else if (parameterType.equals(Class.class)) {
                    result = Class.forName(jsonElement.getAsString());
                } else if (parameterType.equals(UUID.class)) {
                    result = UUID.fromString(jsonElement.getAsString());
                } else if (parameterType.equals(Date.class)) {
                    for(String dateFormat : SystemProperties.getList(SystemProperties.Net.Http.EndPoint.Json.DATE_FORMATS)) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
                        try {
                            simpleDateFormat.applyPattern(dateFormat);
                            result = simpleDateFormat.format(jsonElement.getAsString());
                        } catch (Exception ex){}
                    }
                } else if (parameterType.equals(Byte.class) || parameterType.equals(byte.class)) {
                    result = jsonElement.getAsNumber().byteValue();
                } else if (parameterType.equals(Short.class) || parameterType.equals(short.class)) {
                    result = jsonElement.getAsNumber().shortValue();
                } else if (parameterType.equals(Integer.class) || parameterType.equals(int.class)) {
                    result = jsonElement.getAsNumber().intValue();
                } else if (parameterType.equals(Long.class) || parameterType.equals(long.class)) {
                    result = jsonElement.getAsNumber().longValue();
                } else if (parameterType.equals(Float.class) || parameterType.equals(float.class)) {
                    result = jsonElement.getAsNumber().floatValue();
                } else if (parameterType.equals(Double.class) || parameterType.equals(double.class)) {
                    result = jsonElement.getAsNumber().doubleValue();
                } else if (parameterType.equals(Boolean.class) || parameterType.equals(boolean.class)) {
                    result = jsonElement.getAsBoolean();
                }
            } catch (Exception ex){
                Log.w(SystemProperties.Net.Http.LOG_TAG, "Unable to encode value %s to %s data type",
                        jsonElement.toString(), parameterType.toString());
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

    class RestReference implements References {

        private final Map<String, Object> references;

        public RestReference() {
            this.references = new HashMap<>();
        }

        @Override
        public <O> O getReference(String referenceName) {
            O result = null;
            if(references.containsKey(referenceName)) {
                Object referenceValue = references.get(referenceName);
                if (referenceValue instanceof Collection) {
                    result = (O) ((Collection) referenceValue).iterator().next();
                } else {
                    result = (O) referenceValue;
                }
            }
            return result;
        }

        public <O> Collection<O> getReferenceCollection(String referenceName) {
            return (Collection<O>) references.get(referenceName);
        }

        public void putReference(String referenceName, Object reference) {
            references.put(referenceName, reference);
        }

        public boolean isEmpty() {
            return references.isEmpty();
        }
    }
}
