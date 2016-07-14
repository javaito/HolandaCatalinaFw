package org.hcjf.encoding;

import com.google.gson.*;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.nio.charset.Charset;
import java.util.*;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class JsonEncoding extends EncodingImpl {

    private static final String HCJF_JSON_IMPLEMENTATION = "hcjf";

    private static final String PARAMETERS_JSON_FIELD = "params";
    private static final String BODY_JSON_FIELD = "body";
    private static final String TYPE_PARAMETER_FIELD = "t";
    private static final String VALUE_PARAMETER_FIELD = "v";

    public JsonEncoding() {
        super(MimeType.APPLICATION_JSON, HCJF_JSON_IMPLEMENTATION);
    }

    /**
     * @param decodedPackage
     * @return
     */
    @Override
    public byte[] encode(DecodedPackage decodedPackage) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();

        if(decodedPackage.getParameters() != null) {
            JsonObject parameterObject = new JsonObject();
            EncodingType type;
            JsonObject typedObject;
            for(String key : decodedPackage.getParameters().keySet()) {
                typedObject = new JsonObject();
                type = EncodingType.fromClass(decodedPackage.getParameters().get(key).getClass());
                typedObject.add(TYPE_PARAMETER_FIELD, new JsonPrimitive(type.getId()));
                typedObject.add(VALUE_PARAMETER_FIELD, getElement(type, decodedPackage.getParameters().get(key)));
                parameterObject.add(key, typedObject);
            }
            jsonObject.add(PARAMETERS_JSON_FIELD, parameterObject);
        }

        if(decodedPackage.getObject() != null) {
            if(decodedPackage.getObject() instanceof Collection) {
                JsonArray array = new JsonArray();
                for(Object arrayObject : (Collection)decodedPackage.getObject()) {
                    array.add(getBodyObject(arrayObject));
                }
                jsonObject.add(BODY_JSON_FIELD, array);
            } else {
                jsonObject.add(BODY_JSON_FIELD, getBodyObject(decodedPackage.getObject()));
            }
        }

        return gson.toJson(jsonObject).getBytes();
    }

    public JsonObject getBodyObject(Object object) {
        JsonObject bodyObject = new JsonObject();
        EncodingType type;
        JsonObject typedObject;
        Object value;
        Map<String, Introspection.Getter> getters = Introspection.getGetters(object.getClass());
        for(String fieldName : getters.keySet()) {
            try {
                value = getters.get(fieldName).invoke(object);
                if(value == null) {
                    continue;
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("", ex);
            }
            typedObject = new JsonObject();
            type = EncodingType.fromClass(value.getClass());
            typedObject.add(TYPE_PARAMETER_FIELD, new JsonPrimitive(type.getId()));
            typedObject.add(VALUE_PARAMETER_FIELD, getElement(type, value));
            bodyObject.add(fieldName, typedObject);
        }
        return bodyObject;
    }

    public JsonElement getElement(EncodingType type, Object value) {
        JsonElement element = null;

        switch (type) {
            case BOOLEAN: element = new JsonPrimitive((Boolean)value); break;
            case BYTE: element = new JsonPrimitive((Byte)value); break;
            case DATE: element = new JsonPrimitive(((Date)value).getTime()); break;
            case DOUBLE: element = new JsonPrimitive((Double)value); break;
            case FLOAT: element = new JsonPrimitive((Float)value); break;
            case INTEGER: element = new JsonPrimitive((Integer)value); break;
            case LONG: element = new JsonPrimitive((Long)value); break;
            case SHORT: element = new JsonPrimitive((Short)value); break;
            case STRING: element = new JsonPrimitive((String)value); break;
            case UUID: element = new JsonPrimitive(value.toString()); break;
            case  LIST: {
                JsonArray array = new JsonArray();
                JsonObject typedObject;
                for(Object arrayValue : (List)value) {
                    typedObject = new JsonObject();
                    type = EncodingType.fromClass(value.getClass());
                    typedObject.add(TYPE_PARAMETER_FIELD, new JsonPrimitive(type.getId()));
                    typedObject.add(TYPE_PARAMETER_FIELD, getElement(type, arrayValue));
                    array.add(typedObject);
                }
                element = array;
                break;
            }
            case MAP: {
                JsonObject map = new JsonObject();
                JsonObject typedObject;
                for(String name : ((Map<String, ?>)value).keySet()){
                    typedObject = new JsonObject();
                    type = EncodingType.fromClass(((Map<String, ?>)value).get(name).getClass());
                    typedObject.add(TYPE_PARAMETER_FIELD, new JsonPrimitive(type.getId()));
                    typedObject.add(TYPE_PARAMETER_FIELD, getElement(type, ((Map<String, ?>)value).get(name)));
                    map.add(name, typedObject);
                }
                element = map;
                break;
            }
            case BYTE_BUFFER: throw new UnsupportedOperationException("Byte buffer type is not supported for 'HCJF' json encoding");
        }

        return element;
    }

    /**
     * @param data
     * @param parameters
     * @return
     */
    @Override
    public DecodedPackage decode(byte[] data, Map<String, Object> parameters) {
        return decode(null, data, parameters);
    }

    /**
     * @param objectClass
     * @param data
     * @param parameters
     * @return
     */
    @Override
    public DecodedPackage decode(Class objectClass, byte[] data, Map<String, Object> parameters) {
        String charset = SystemProperties.getDefaultCharset();
        if(parameters.containsKey(CHARSET_PARAMETER_NAME)) {
            charset = (String) parameters.get(CHARSET_PARAMETER_NAME);
        }
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new String(data, Charset.forName(charset)));
        if(!jsonElement.isJsonObject()) {
            throw new IllegalArgumentException("The HCJF json implementation expected a json object like data");
        }
        JsonObject jsonData = (JsonObject) jsonElement;

        Object decodedObject = null;
        Map<String, Object> decodedParameters = new HashMap<>();
        if(jsonData.has(PARAMETERS_JSON_FIELD)) {
            if(jsonData.get(PARAMETERS_JSON_FIELD).isJsonObject()) {
                JsonObject jsonParams = jsonData.get(PARAMETERS_JSON_FIELD).getAsJsonObject();
                for(Map.Entry<String, JsonElement> entry : jsonParams.entrySet()) {
                    if(entry.getValue().isJsonObject()) {
                        decodedParameters.put(entry.getKey(), getValue(entry.getKey(), entry.getValue()));
                    } else if(entry.getValue().isJsonPrimitive()) {
                        decodedParameters.put(entry.getKey(), entry.getValue().getAsString());
                    } else {
                        throw new IllegalArgumentException("The HCJF json implementation expected parameter values as json object or as json primitive");
                    }
                }
            } else {
                throw new IllegalArgumentException("The HCJF json implementation expected " + PARAMETERS_JSON_FIELD + " field as json object");
            }
        }

        if(objectClass != null) {
            if (jsonData.has(BODY_JSON_FIELD)) {
                if(jsonData.get(BODY_JSON_FIELD).isJsonObject()) {
                    decodedObject = getInstance(objectClass, jsonData.get(BODY_JSON_FIELD));
                } else if(jsonData.get(BODY_JSON_FIELD).isJsonArray()) {
                    List<Object> resultList = new ArrayList<>();
                    for(JsonElement element : ((JsonArray)jsonData.get(BODY_JSON_FIELD))) {
                        resultList.add(getInstance(objectClass, element));
                    }
                    decodedObject = resultList;
                } else {
                    throw new IllegalArgumentException("The json field " + BODY_JSON_FIELD + " must be json object or json array");
                }
            } else {
                throw new IllegalArgumentException("Unable to create instance of " + objectClass + " because body field not found in json object");
            }
        }

        return new DecodedPackage(decodedObject, decodedParameters);
    }

    protected Object getInstance(Class objectClass, JsonElement jsonElement) {
        Object result;
        if(jsonElement.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            try {
                result = objectClass.newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to create instance of " + objectClass, ex);
            }

            Map<String, Introspection.Setter> setters = Introspection.getSetters(objectClass);
            for(String fieldName : setters.keySet()) {
                if(jsonObject.has(fieldName)) {
                    try {
                        setters.get(fieldName).invoke(result, getValue(fieldName, jsonObject.get(fieldName)));
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Unable to set field " + fieldName, ex);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("The HCJF json implementation expected " + BODY_JSON_FIELD + " field as json object");
        }
        return result;
    }

    protected Object getValue(String fieldName, JsonElement jsonElement) {
        Object result;
        if(jsonElement.isJsonObject() &&
                ((JsonObject)jsonElement).has(TYPE_PARAMETER_FIELD) &&
                ((JsonObject)jsonElement).has(VALUE_PARAMETER_FIELD)) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            EncodingType encodingType;
            try {
                encodingType = EncodingType.fromId(jsonObject.get(TYPE_PARAMETER_FIELD).getAsByte());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unsupported encoding type for field " + fieldName);
            }
            JsonElement value = jsonObject.get(VALUE_PARAMETER_FIELD);
            result = getValue(fieldName, encodingType, value);
        } else {
            throw new IllegalArgumentException("The field " + fieldName + " expected as json array of json object, with internal format '{t:typeByte,v:value}'");
        }
        return result;
    }

    protected Object getValue(String fieldName, EncodingType type, JsonElement jsonElement) {
        Object result = null;

        switch(type) {
            case LIST: {
                if(jsonElement.isJsonArray()) {
                    JsonArray jsonArray = (JsonArray) jsonElement;
                    List<Object> resultList = new ArrayList<>();
                    int index = 0;
                    for(JsonElement listElement : jsonArray) {
                        resultList.add(getValue(fieldName + "->" + index++, listElement));
                    }
                    result = resultList;
                } else {
                    throw new IllegalArgumentException("The field " + fieldName + " expected as json array");
                }
                break;
            }
            case MAP: {
                if(jsonElement.isJsonObject()) {
                    JsonObject mapJsonObject = (JsonObject) jsonElement;
                    Map<String, Object> resultMap = new HashMap<>();
                    for(Map.Entry<String, JsonElement> entry : mapJsonObject.entrySet()) {
                        resultMap.put(entry.getKey(), getValue(fieldName + "->" + entry.getKey(), entry.getValue()));
                    }
                    result = resultMap;
                } else {
                    throw new IllegalArgumentException("The field " + fieldName + " expected as json object");
                }
                break;
            }
            case BOOLEAN: try { result = jsonElement.getAsBoolean(); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as boolean", ex);} ;break;
            case BYTE: try { result = jsonElement.getAsByte(); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as byte", ex);} ;break;
            case DATE: try { result = new Date(jsonElement.getAsLong()); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as date", ex);} ;break;
            case DOUBLE: try { result = jsonElement.getAsDouble(); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as double", ex);} ; break;
            case FLOAT: try { result = jsonElement.getAsFloat(); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as float", ex);} ; break;
            case INTEGER: try { result = jsonElement.getAsInt(); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as integer", ex);} ; break;
            case LONG: try { result = jsonElement.getAsLong(); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as long", ex);} ; break;
            case SHORT: try { result = jsonElement.getAsShort(); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as short", ex);} ; break;
            case STRING: try { result = jsonElement.getAsString(); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as string", ex);} ; break;
            case UUID: try { result = UUID.fromString(jsonElement.getAsString()); } catch (Exception ex) {throw new IllegalArgumentException("The field " + fieldName + " expected as string", ex);} ; break;
            case BYTE_BUFFER: throw new UnsupportedOperationException("Byte buffer type is not supported for 'HCJF' json encoding");
        }

        return result;
    }

}
