package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.properties.SystemProperties;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * This class represents a standard web context that can be published
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class Context {

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
    public String getContextRegex() {
        return contextRegex;
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
        response.setReasonPhrase(throwable.getMessage());
        response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);

        byte[] body;
        if(SystemProperties.getBoolean(SystemProperties.HTTP_DEFAULT_ERROR_FORMAT_SHOW_STACK)) {
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
