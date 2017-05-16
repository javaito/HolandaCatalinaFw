package org.hcjf.io.net.http;

import com.google.gson.JsonObject;
import org.hcjf.encoding.MimeType;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This kind of context publish the enums of the system.
 * @author javaito
 */
public class EnumContext extends Context{

    private final String name;
    private final String[] names;

    public EnumContext(String name) {
        super(START_CONTEXT + URI_FOLDER_SEPARATOR + name + END_CONTEXT);
        this.name = name;
        this.names = name.split(URI_FOLDER_SEPARATOR);
    }

    @Override
    public HttpResponse onContext(HttpRequest request) {
        List<String> elements = request.getPathParts();

        String separator = Strings.EMPTY_STRING;
        StringBuilder enumClassNameBuilder = new StringBuilder();
        for(String element : elements) {
            if(!element.isEmpty() && Arrays.binarySearch(names, element) < 0) {
                enumClassNameBuilder.append(separator);
                enumClassNameBuilder.append(element);
                separator = Strings.CLASS_SEPARATOR;
            }
        }

        String enumClassName = enumClassNameBuilder.toString();
        Class enumClass = null;
        try {
            enumClass = Class.forName(enumClassName);
        } catch (ClassNotFoundException e) {}

        HttpResponse response = new HttpResponse();

        if(enumClass != null && enumClass.isEnum()) {
            JsonObject jsonBody = new JsonObject();
            JsonObject jsonInstance;
            Map<String,String> map;
            for(Object enumInstance : enumClass.getEnumConstants()) {
                jsonInstance = new JsonObject();
                map = Introspection.toStringsMap(enumInstance);
                for(String key : map.keySet()) {
                    jsonInstance.addProperty(key, map.get(key));
                }
                jsonBody.add(enumInstance.toString(), jsonInstance);
            }

            byte[] body = jsonBody.toString().getBytes();

            response.setResponseCode(HttpResponseCode.OK);
            response.setReasonPhrase("OK");
            response.setBody(body);
            response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
            response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
        } else {
            response.setResponseCode(HttpResponseCode.NOT_FOUND);
            response.setReasonPhrase("Enum class not found: " + enumClassName);
        }

        return response;
    }

}
