package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.properties.SystemProperties;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Set;

/**
 * This class represents a standard web context that can be published
 * @author javaito
 */
public abstract class Context {

    protected static final String START_CONTEXT = "^";
    protected static final String END_CONTEXT = ".*";
    protected static final String URI_FOLDER_SEPARATOR = "/";

    private final String contextRegex;

    /**
     * Constructor
     * @param contextRegex Regular expression that represents the add
     * of URLs that refer to this context.
     */
    public Context(String contextRegex) {
        this.contextRegex = contextRegex;
    }

    /**
     * Return the regular expression that represents the add of URLs
     * that refer to this context.
     * @return Regular expression.
     */
    public final String getContextRegex() {
        return contextRegex;
    }

    /**
     * It returns a CORS headers set for this context. The headers are used for the CORS preflight request and the successive request.
     * Overwrite this method to implement a particular cross-origin restriction
     * <br><br>
     * More information at <a href="https://www.w3.org/TR/cors/#access-control-allow-origin-response-header">www.w3.org</a>
     * <br>
     * Usage guide <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS">developer.mozilla.org</a>
     *
     * @param request A request that has the origin header present
     * @return A headers list to add in the HttpResponse
     */
    protected Set<HttpHeader> getCrossOriginHeaders(HttpRequest request){
        return Collections.EMPTY_SET;
    }

    /**
     * This method is called when there comes a http package addressed to this
     * context.
     * @param request All the request information.
     * @return Return an object with all the response information.
     */
    public abstract HttpResponse onContext(HttpRequest request);

    /**
     * This method is called when there are any error on the context execution.
     * @param request All the request information.
     * @param throwable Throwable object, could be null.
     * @return Return an object with all the response information.
     */
    protected HttpResponse onError(HttpRequest request, Throwable throwable) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);

        byte[] body;
        if(SystemProperties.getBoolean(SystemProperties.Net.Http.DEFAULT_ERROR_FORMAT_SHOW_STACK)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream printer = new PrintStream(out);
            throwable.printStackTrace(printer);
            body = out.toByteArray();
        } else {
            body = throwable.getMessage().getBytes();
        }
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Long.toString(body.length)));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN.toString()));
        response.setBody(body);

        return response;
    }
}
