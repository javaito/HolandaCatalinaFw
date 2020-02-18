package org.hcjf.utils;

import com.google.gson.*;
import org.hcjf.properties.SystemProperties;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static final JsonParser jsonParser;
    private static final Gson gson;
    public static final String DATE_FORMAT_ARG = "dateFormatArgument";
    public static final String ADAPTERS_ARG = "adaptersArgument";

    static {
        jsonParser = new JsonParser();
        gson = new GsonBuilder().setPrettyPrinting().setDateFormat(
                SystemProperties.get(SystemProperties.HCJF_DEFAULT_DATE_FORMAT)).create();
    }

    /**
     * Creates a instance from a json definition.
     * @param json Json definition.
     * @return Object instance.
     */
    public static Object createObject(String json) {
        return createObject(jsonParser.parse(json));
    }

    /**
     * Creates the body object from a json object.
     * @param jsonObject Json object instance.
     * @return Map with all the fields of the object.
     */
    public static Map<String,Object> createBody(JsonObject jsonObject) {
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
    public static List<Object> createList(JsonArray array) {
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
    public static Object createObject(JsonElement element) {
        Object value;
        if(element instanceof JsonObject) {
            value = createBody((JsonObject) element);
        } else if(element instanceof JsonArray) {
            value = createList((JsonArray) element);
        } else if(element instanceof JsonPrimitive && ((JsonPrimitive)element).isString()) {
            value = Strings.deductInstance(element.getAsString());

            //This control is to save the case when the value into the json file is marked with quotes
            if (Number.class.isAssignableFrom(value.getClass()) || Boolean.class.isAssignableFrom(value.getClass())) {
                value = element.getAsString();
            }
        } else if(element instanceof JsonNull) {
            value = null;
        } else {
            value = Strings.deductInstance(element.getAsString());
        }
        return value;
    }

    public static JsonElement toJsonTree(Object object) {
        return gson.toJsonTree(object);
    }

    public static JsonElement toJsonTree(Object object, Map<String, Object> formatOptions) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        formatOptions.forEach((key,value) -> {
            switch (key){
                case DATE_FORMAT_ARG : {
                    gsonBuilder.setPrettyPrinting().setDateFormat(value.toString());
                    break;
                }
                case ADAPTERS_ARG: {
                    Map<Type, Object> adapters = (Map<Type, Object>) value;
                    adapters.forEach((type, adapter) ->{
                        gsonBuilder.setPrettyPrinting().registerTypeAdapter(type, adapter);
                    });
                    break;
                }
                default:{break;}
            }
        });
        return gsonBuilder.create().toJsonTree(object);
    }
}
