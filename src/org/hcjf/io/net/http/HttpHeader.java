package org.hcjf.io.net.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a http header and contains all
 * the components of the headers.
 * @author javaito
 * @email javaito@gmail.com
 */
public class HttpHeader {

    //Header names
    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String AUTHORIZATION = "Authorization";
    public static final String EXPECT = "Expect";
    public static final String FROM = "From";
    public static final String HOST = "Host";
    public static final String IF_MATCH = "If-Match";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String IF_NONE_MATCH = "If-None-Match";
    public static final String IF_RANGE = "If-Range";
    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    public static final String MAX_FORWARDS = "Max-Forwards";
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static final String RANGE = "Range";
    public static final String REFERER = "Referer";
    public static final String TE = "TE";
    public static final String USER_AGENT = "User-Agent";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String SERVER = "Server";
    public static final String DATE = "Date";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String CONNECTION = "Connection";

    //Header values
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CLOSED = "Closed";

    private static final char HEADER_ASIGNATION = ':';

    //Values in order to parse content-type header
    protected static final String CONTENT_TYPE_FIELD_SEPARATOR = ";";
    protected static final String CONTENT_TYPE_FIELD_ASSIGNATION = "=";

    //Header parameter names
    public static final String CHARSET = "charset";

    private final String headerName;
    private final String headerValue;
    private final Map<String, String> parameters;

    public HttpHeader(String header) {
        this(header.substring(0, header.indexOf(HEADER_ASIGNATION)),
                header.substring(header.indexOf(HEADER_ASIGNATION) + 1));
    }

    public HttpHeader(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
        this.parameters = new HashMap<>();
        parse();
    }

    /**
     * Return the header name.
     * @return Header name.
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * Return the header name.
     * @return Header name.
     */
    public String getHeaderValue() {
        return headerValue;
    }

    /**
     * Print the header with the http header standard format.
     * @return Header's print.
     */
    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Print the header with the http header standard format.
     * @param includeHeaderName
     * @return Header's print.
     */
    public String toString(boolean includeHeaderName) {
        StringBuilder result = new StringBuilder();
        if(includeHeaderName) {
            result.append(getHeaderName());
            result.append(HEADER_ASIGNATION).append(" ");
        }
        result.append(getHeaderValue());
        return result.toString();
    }

    /**
     *
     * @param parameterName
     * @return
     */
    public final String getParameter(String parameterName) {
        return parameters.get(parameterName);
    }

    /**
     *
     */
    private void parse() {
        switch (headerValue) {
            case CONTENT_TYPE: {
                parseContentType();
                break;
            }
        }
    }

    /**
     *
     */
    private void parseContentType() {
        if(headerValue.indexOf(CONTENT_TYPE_FIELD_SEPARATOR) >= 0) {
            String[] fields = headerValue.split(CONTENT_TYPE_FIELD_SEPARATOR);
            String[] name_value;
            for(int i = 1; i < fields.length; i++) {
                if(fields[i].indexOf(CONTENT_TYPE_FIELD_ASSIGNATION) >= 0) {
                    name_value = fields[i].split(CONTENT_TYPE_FIELD_ASSIGNATION);
                    parameters.put(name_value[0], name_value[1]);
                }
            }
        }
    }
}
