package org.hcjf.io.net.http;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This particular kind of package contains the request information.
 * @author javaito
 * @email javaito@gmail.com
 */
public class HttpRequest extends HttpPackage {

    private static final int METHOD_INDEX = 0;
    private static final int REQUEST_PATH_INDEX = 1;
    private static final int VERSION_INDEX = 2;

    private String path;
    private String context;
    private HttpMethod method;
    private final Map<String, String> parameters;

    public HttpRequest(String requestPath, HttpMethod method) {
        this.path = requestPath;
        this.method = method;
        this.parameters = new HashMap<>();
    }

    public HttpRequest() {
        this(null, null);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public String getParameter(String parameterName) {
        return parameters.get(parameterName);
    }

    public void addHttpParameter(String parameterName, String parameterValue) {
        parameters.put(parameterName, parameterValue);
    }

    /**
     * @param body
     * @return
     */
    @Override
    protected void processBody(byte[] body) {
        HttpHeader contentType = getHeader(HttpHeader.CONTENT_TYPE);
        if(contentType != null && contentType.getHeaderValue().equals(HttpHeader.APPLICATION_X_WWW_FORM_URLENCODED)) {
            parseHttpParameters(new String(body));
        } else {

        }
    }

    /**
     * @param firstLine
     */
    @Override
    protected void processFirstLine(String firstLine) {
        String[] parts = firstLine.split(LINE_FIELD_SEPARATOR);

        method = HttpMethod.valueOf(parts[METHOD_INDEX]);
        path = parts[REQUEST_PATH_INDEX];
        setHttpVersion(parts[VERSION_INDEX]);

        //Check if there are parameters into request
        if(path.indexOf(HTTP_FIELD_START) >= 0) {
            context = path.substring(0, path.indexOf(HTTP_FIELD_START));
            parseHttpParameters(path.substring(path.indexOf(HTTP_FIELD_START) + 1));
        } else {
            context = path;
        }
    }

    /**
     *
     * @param parametersBody
     */
    private void parseHttpParameters(String parametersBody) {
        String[] params = parametersBody.split(HTTP_FIELD_SEPARATOR);

        String charset = null;
        HttpHeader contentType = getHeader(HttpHeader.CONTENT_TYPE);
        if(contentType != null) {
            charset = contentType.getParameter(HttpHeader.CHARSET);
        }
        if(charset == null) {
            charset = SystemProperties.getDefaultCharset();
        }

        String key;
        String value;
        for(String param : params) {
            if(param.indexOf(HTTP_FIELD_ASSIGNATION) < 0) {
                key = param;
                value = null;
            } else {
                String[] keyValue = param.split(HTTP_FIELD_ASSIGNATION);
                key = keyValue[0];
                value = keyValue[1];
            }

            try {
                parameters.put(URLDecoder.decode(key, charset), value == null ? null : URLDecoder.decode(value, charset));
            } catch (UnsupportedEncodingException e) {
                Log.w(HttpServer.HTTP_SERVER_LOG_TAG, "Unable to decode http parameter, %s:%s", key, value);
                parameters.put(key, value);
            }
        }
    }
}
