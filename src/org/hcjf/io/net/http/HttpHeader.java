package org.hcjf.io.net.http;

import org.hcjf.errors.Errors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String SERVER = "Server";
    public static final String DATE = "Date";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String CONNECTION = "Connection";
    public static final String E_TAG = "ETag";

    //Header values
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CLOSED = "Closed";
    public static final String DEFAULT_USER_AGENT = "HCJF";
    public static final String DEFLATE = "deflate";
    public static final String GZIP = "gzip";
    public static final String IDENTITY = "identity";

    private static final String HEADER_ASSIGNATION = ":";
    private static final String HEADER_GROUPS_SEPARATOR = ",";
    private static final String HEADER_FIELDS_SEPARATOR = ";";
    private static final String HEADER_FIELDS_ASSIGNATION = "=";

    //Header parameter names
    public static final String PARAM_CHARSET = "charset";
    public static final String PARAM_IMPL = "impl";

    private final String headerName;
    private final String headerValue;
    private final Map<String, Map<String, String>> headerGroups;

    public HttpHeader(String header) {
        this(header.substring(0, header.indexOf(HEADER_ASSIGNATION)).trim(),
                header.substring(header.indexOf(HEADER_ASSIGNATION) + 1).trim());
    }

    public HttpHeader(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
        this.headerGroups = new HashMap<>();
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
            result.append(HEADER_ASSIGNATION).append(" ");
        }
        result.append(getHeaderValue());
        return result.toString();
    }

    /**
     *
     * @return
     */
    public final Set<String> getGroups() {
        return headerGroups.keySet();
    }

    /**
     *
     * @param groupName
     * @param parameterName
     * @return
     */
    public final String getParameter(String groupName, String parameterName) {
        if(groupName == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_7));
        }

        if(parameterName == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_8));
        }

        return headerGroups.get(groupName).get(parameterName);
    }

    /**
     *
     */
    private void parse() {
        if(headerName.equalsIgnoreCase(ACCEPT) ||
                headerName.equalsIgnoreCase(CONTENT_TYPE) ||
                headerName.equalsIgnoreCase(ACCEPT_ENCODING)) {
            parseStandardGroup();
        }
    }

    /**
     *
     */
    private void parseStandardGroup() {
        String[] groups = headerValue.split(HEADER_GROUPS_SEPARATOR);
        Map<String, String> groupParameters;
        for(String group : groups) {
            String[] fields = group.split(HEADER_FIELDS_SEPARATOR);
            groupParameters = new HashMap<>();
            headerGroups.put(fields[0].trim(), groupParameters);
            String[] name_value;
            for (int i = 1; i < fields.length; i++) {
                if (fields[i].indexOf(HEADER_FIELDS_ASSIGNATION) >= 0) {
                    if (fields[i].indexOf(HEADER_FIELDS_ASSIGNATION) >= 0) {
                        name_value = fields[i].split(HEADER_FIELDS_ASSIGNATION);
                        groupParameters.put(name_value[0].trim(), name_value[1].trim());
                    } else {
                        groupParameters.put(fields[i].trim(), "");
                    }
                }
            }
        }
    }

}
