package org.hcjf.io.net.http;

import org.hcjf.layers.Layer;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This layer implementation contains the logic to decode a body
 * using the form url encoding standard method, that correspond with
 * the content type header 'application/x-www-form-urlencoded'
 * @author javaito
 */
public class FormUrlEncodedDecoder extends Layer implements RequestBodyDecoderLayer {

    public FormUrlEncodedDecoder() {
        super(HttpHeader.APPLICATION_X_WWW_FORM_URLENCODED);
    }

    /**
     * Decodes the request body that must be encoding with url encoding method.
     * @param request Http request instance.
     * @return Returns the map with all the decoded parameters.
     */
    @Override
    public Map<String, Object> decode(HttpRequest request) {
        Map<String,Object> parameters = new HashMap<>();
        String[] params = new String(request.getBody()).split(HttpPackage.HTTP_FIELD_SEPARATOR);

        String charset = null;
        HttpHeader contentType = request.getHeader(HttpHeader.CONTENT_TYPE);
        if(contentType != null) {
            //The content-type header should not have more than one group
            //In this case we use the first group.
            charset = contentType.getParameter(
                    contentType.getGroups().iterator().next(), HttpHeader.PARAM_CHARSET);
        }
        if(charset == null) {
            charset = SystemProperties.getDefaultCharset();
        }

        String key;
        String value;
        int insertIndex;
        ArrayList<String> listParameter;
        for(String param : params) {
            if(param.indexOf(HttpPackage.HTTP_FIELD_ASSIGNATION) < 0) {
                key = param;
                value = null;
            } else {
                String[] keyValue = param.split(HttpPackage.HTTP_FIELD_ASSIGNATION);
                key = keyValue[0];
                value = keyValue.length==2 ? keyValue[1] : null;
            }

            if(key.contains(Strings.START_SUB_GROUP) && key.contains(Strings.END_SUB_GROUP)) {
                insertIndex = -1;
                if(key.indexOf(Strings.START_SUB_GROUP) + 1 == key.indexOf(Strings.END_SUB_GROUP)) {
                } else {
                    insertIndex = Integer.parseInt(key.substring(key.indexOf(Strings.START_SUB_GROUP) + 1, key.indexOf(Strings.END_SUB_GROUP)));
                }

                key = key.substring(0, key.indexOf(Strings.START_SUB_GROUP));
                if(parameters.containsKey(key)) {
                    listParameter = (ArrayList<String>) parameters.get(key);
                } else {
                    listParameter = new ArrayList<>();
                    parameters.put(key, listParameter);
                }

                if(insertIndex >= 0) {
                    listParameter.add(insertIndex, value);
                } else {
                    listParameter.add(value);
                }
            } else {
                try {
                    parameters.put(URLDecoder.decode(key, charset), value == null ? null : URLDecoder.decode(value, charset));
                } catch (UnsupportedEncodingException e) {
                    Log.w(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Unable to decode http parameter, %s:%s", key, value);
                    parameters.put(key, value);
                }
            }
        }
        return parameters;
    }

}
