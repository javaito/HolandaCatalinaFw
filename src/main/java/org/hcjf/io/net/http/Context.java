package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.properties.SystemProperties;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * This class represents a standard web context that can be published
 * @author javaito
 */
public abstract class Context {

    protected static final String START_CONTEXT = "^";
    protected static final String END_CONTEXT = ".*";
    protected static final String URI_FOLDER_SEPARATOR = "/";

    private final Pattern pattern;
    private final String contextRegex;
    private long timeout;

    /**
     * Constructor
     * @param contextRegex Regular expression that represents the add
     * of URLs that refer to this context.
     */
    public Context(String contextRegex) {
        this.pattern = Pattern.compile(contextRegex);
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
     * Returns the pattern created with the regex of the context.
     * @return Pattern instance.
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * This method is called when there comes a http package addressed to this
     * context.
     * @param request All the request information.
     * @return Return an object with all the response information.
     */
    public abstract HttpResponse onContext(HttpRequest request);

    /**
     * Returns the timeout value of the context execution.
     * @return Timeout value.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Set the timeout value of the context execution.
     * @param timeout Timeout value.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * This method is called when there are any error on the context execution.
     * @param request All the request information.
     * @param throwable Throwable object, could be null.
     * @return Return an object with all the response information.
     */
    protected HttpResponse onError(HttpRequest request, Throwable throwable) {
        return createDefaultErrorResponse(throwable);
    }

    /**
     * Utils method in order to reuse the creation of error response package.
     * @param throwable Throwable instance.
     * @return Http response instance.
     */
    public static HttpResponse createDefaultErrorResponse(Throwable throwable) {
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

    /**
     * Utils method in order to add a default headers into the default response package.
     * @param response Response package.
     * @param body Body object.
     * @return Returns the same response instance with the headers added.
     */
    public static HttpResponse addDefaultResponseHeaders(HttpResponse response, byte[] body) {
        response.setBody(body);
        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN.toString()));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
        return response;
    }
}
