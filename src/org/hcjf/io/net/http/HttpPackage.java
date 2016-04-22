package org.hcjf.io.net.http;

import java.util.*;

/**
 * This class represents all the king of packages between server and
 * client side in a http comunication.
 * @author javaito
 * @email javaito@gmail.com
 */
public class HttpPackage {

    private final Map<String, HttpHeader> headers;
    private byte[] body;

    public HttpPackage() {
        this.headers = new HashMap<>();
    }

    /**
     * Return the body of the package.
     * @return Body.
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Set the body of the package.
     * @param body Body.
     */
    public void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * Add a new header into a package.
     * @param header New header.
     */
    public void addHeader(HttpHeader header) {
        if(header == null) {
            throw new NullPointerException("Null header");
        }
        headers.put(header.getHeaderName(), header);
    }

    /**
     * Return in a unmodificable list, all the headers contained
     * into the package.
     * @return List of the headers.
     */
    public Collection<HttpHeader> getHeaders() {
        return Collections.unmodifiableCollection(headers.values());
    }
}
