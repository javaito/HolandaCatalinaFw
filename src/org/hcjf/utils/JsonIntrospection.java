package org.hcjf.utils;

import com.google.gson.JsonElement;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public final class JsonIntrospection {

    private static final String PATH_SEPARATOR = "\\.";
    private static final String TO_STRING_PATH_SEPARATOR = "->";

    /**
     *
     * @param element
     * @param path
     * @param returnType
     * @param <O>
     * @return
     */
    public static <O extends Object> O getValue(JsonElement element, String path, Class<? extends O> returnType) {
        O result = null;
        String[] places = path.split(PATH_SEPARATOR);
        JsonElement currentElement = element;
        StringBuilder currentPath = new StringBuilder();
        int arrayIndex;
        for(String place : places) {
            if(currentElement.isJsonObject()) {
                currentElement = currentElement.getAsJsonObject().get(place);
            } else if(currentElement.isJsonArray()) {
                try {
                    arrayIndex = Integer.parseInt(place);
                } catch(Exception ex) {
                    throw new IllegalArgumentException("Expected array index after current path " + currentPath + " (" + place + ")");
                }
                currentElement = currentElement.getAsJsonArray().get(arrayIndex);
            } else {
                throw new IllegalArgumentException("Current element is not a collection object: " + currentPath);
            }
            currentPath.append(TO_STRING_PATH_SEPARATOR).append(place);
        }

        if(List.class.isAssignableFrom(returnType)) {
            if(currentElement.isJsonArray()) {
                List<String> list = new ArrayList<>();
                currentElement.getAsJsonArray().forEach(E -> list.add(E.getAsString()));
                result = (O) list;
            } else {
                throw new IllegalArgumentException("Expected to be an array element found");
            }
        } else if(Map.class.isAssignableFrom(returnType)) {
            if(currentElement.isJsonObject()) {
                Map<String, String> map = new HashMap<>();
                currentElement.getAsJsonObject().entrySet().forEach(E -> map.put(E.getKey(), E.getValue().getAsString()));
                result = (O) map;
            } else {
                throw new IllegalArgumentException("Expected to be an json object element found");
            }
        } else if(Boolean.class.isAssignableFrom(returnType)) {
            result = (O) new Boolean(currentElement.getAsBoolean());
        } else if(Byte.class.isAssignableFrom(returnType)) {
            result = (O) new Byte(currentElement.getAsByte());
        } else if(Integer.class.isAssignableFrom(returnType)) {
            result = (O) new Integer(currentElement.getAsInt());
        } else if(Short.class.isAssignableFrom(returnType)) {
            result = (O) new Short(currentElement.getAsShort());
        } else if(Long.class.isAssignableFrom(returnType)) {
            result = (O) new Long(currentElement.getAsLong());
        } else if(Float.class.isAssignableFrom(returnType)) {
            result = (O) new Float(currentElement.getAsFloat());
        } else if(Double.class.isAssignableFrom(returnType)) {
            result = (O) new Double(currentElement.getAsDouble());
        } else if(Date.class.isAssignableFrom(returnType)) {
            result = (O) new Date(currentElement.getAsLong());
        } else if(String.class.isAssignableFrom(returnType)) {
            result = (O) currentElement.getAsString();
        } else if(java.util.UUID.class.isAssignableFrom(returnType)) {
            result = (O) UUID.fromString(currentElement.getAsString());
        } else if(Pattern.class.isAssignableFrom(returnType)) {
            result = (O) Pattern.compile(currentElement.getAsString());
        } else {
            throw new IllegalArgumentException("Unsupported return type: " + returnType);
        }

        return result;
    }

}
