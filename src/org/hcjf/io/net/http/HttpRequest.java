package org.hcjf.io.net.http;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

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
    private final List<String> pathParts;

    public HttpRequest(String requestPath, HttpMethod method) {
        this.path = requestPath;
        this.method = method;
        this.parameters = new HashMap<>();
        this.pathParts = new ArrayList<>();
    }

    protected HttpRequest(HttpRequest httpRequest) {
        super(httpRequest);
        this.path = httpRequest.path;
        this.method = httpRequest.method;
        this.parameters = httpRequest.parameters;
        this.pathParts = httpRequest.pathParts;
    }

    public HttpRequest() {
        this(null, null);
    }

    /**
     * Return the request path
     * @return Request path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the request path.
     * @param path Request path.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Return the request context.
     * @return Request context.
     */
    public String getContext() {
        return context;
    }

    /**
     * Set the request context.
     * @param context Request context.
     */
    public void setContext(String context) {
        this.context = context;
        this.path = context;
    }

    /**
     * Return the request method.
     * @return Request method.
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Set the request method.
     * @param method Request method.
     */
    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    /**
     * Return the request parameters.
     * @return Request parameters.
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Return the parameter indexed by the argument name.
     * @param parameterName Name of the founding parameter.
     * @return Return the parameter.
     */
    public String getParameter(String parameterName) {
        return parameters.get(parameterName);
    }

    /**
     * Add parameter.
     * @param parameterName Parameter name.
     * @param parameterValue Parameter value.
     */
    public void addHttpParameter(String parameterName, String parameterValue) {
        parameters.put(parameterName, parameterValue);
    }

    /**
     * Return a list with all the parts of the request path.
     * If the path is /path1/path2/pathN, then the method response with
     * a list ad [path1,path2,pathN]
     * @return List with all the parts of the request path.
     */
    public List<String> getPathParts() {
        return pathParts;
    }

    /**
     * This method process the body of the complete request.
     * @param body Body of the request.
     */
    @Override
    protected void processBody(byte[] body) {
        HttpHeader contentType = getHeader(HttpHeader.CONTENT_TYPE);
        if(contentType != null &&
                contentType.getHeaderValue().equals(HttpHeader.APPLICATION_X_WWW_FORM_URLENCODED)) {
            parseHttpParameters(new String(body));
        }
    }

    /**
     * @param firstLine
     */
    @Override
    protected void processFirstLine(String firstLine) {
        String[] parts = firstLine.split(LINE_FIELD_SEPARATOR);

        method = HttpMethod.valueOf(parts[METHOD_INDEX]);
        try {
            path = URLDecoder.decode(parts[REQUEST_PATH_INDEX], SystemProperties.getDefaultCharset());
        } catch (UnsupportedEncodingException e) {}
        setHttpVersion(parts[VERSION_INDEX]);

        //Check if there are parameters into request
        if(path.indexOf(HTTP_FIELD_START) >= 0) {
            context = path.substring(0, path.indexOf(HTTP_FIELD_START));
            parseHttpParameters(path.substring(path.indexOf(HTTP_FIELD_START) + 1));
        } else {
            context = path;
        }

        for(String pathPart : context.split(HTTP_CONTEXT_SEPARATOR)) {
            pathParts.add(pathPart);
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
        for(String param : params) {
            if(param.indexOf(HTTP_FIELD_ASSIGNATION) < 0) {
                key = param;
                value = null;
            } else {
                String[] keyValue = param.split(HTTP_FIELD_ASSIGNATION);
                key = keyValue[0];
                value = keyValue.length==2 ? keyValue[1] : null;
            }

            try {
                parameters.put(URLDecoder.decode(key, charset), value == null ? null : URLDecoder.decode(value, charset));
            } catch (UnsupportedEncodingException e) {
                Log.w(HttpServer.HTTP_SERVER_LOG_TAG, "Unable to decode http parameter, %s:%s", key, value);
                parameters.put(key, value);
            }
        }
    }

    /**
     *
     * @return
     */
    private String toStringProtocolHeader() {
        StringBuilder builder = new StringBuilder();

        builder.append(getMethod().toString()).append(LINE_FIELD_SEPARATOR).
                append(getPath()).append(LINE_FIELD_SEPARATOR).
                append(getHttpVersion()).append(STRING_LINE_SEPARATOR);
        for(HttpHeader header : getHeaders()) {
            builder.append(header).append(STRING_LINE_SEPARATOR);
        }
        builder.append(STRING_LINE_SEPARATOR);
        return builder.toString();
    }

    /**
     *
     * @return
     */
    @Override
    public final byte[] getProtocolHeader() {
        return toStringProtocolHeader().getBytes();
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(toStringProtocolHeader());
        if(getBody() != null) {
            int maxLength = SystemProperties.getInteger(SystemProperties.Net.Http.INPUT_LOG_BODY_MAX_LENGTH);
            if(maxLength > 0) {
                if (getBody().length > maxLength) {
                    builder.append(new String(getBody(), 0, maxLength));
                    builder.append(" ... [").append(getBody().length - maxLength).append(" more]");
                } else {
                    builder.append(new String(getBody()));
                }
            }
        }

        return builder.toString();
    }
}
