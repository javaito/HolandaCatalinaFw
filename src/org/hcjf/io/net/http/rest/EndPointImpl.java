package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.HttpResponseCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by javaito on 1/6/2016.
 */
public abstract class EndPointImpl<O extends RestPackage> {

    private final String version;
    private final String format;
    private final Map<Long, HttpRequest> requestMap;

    public EndPointImpl(String version, String format) {
        this.version = version;
        this.format = format;
        this.requestMap = new HashMap<>();
    }

    public String getVersion() {
        return version;
    }

    public String getFormat() {
        return format;
    }

    protected abstract O encode(byte[] body);

    protected abstract byte[] decode(O body);

    public HttpResponse onAction(HttpRequest request) {
        O restPackage = null;
        requestMap.put(Thread.currentThread().getId(), request);
        try {
            switch (request.getMethod()) {
                case GET: {
                    restPackage = get(encode(request.getBody()));
                    break;
                }
                case POST: {
                    restPackage = post(encode(request.getBody()));
                    break;
                }
                case PUT: {
                    restPackage = put(encode(request.getBody()));
                    break;
                }
                case DELETE: {
                    restPackage = delete(encode(request.getBody()));
                    break;
                }
            }
        } finally {
            requestMap.remove(Thread.currentThread().getId());
        }

        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.OK);
        if(restPackage != null) {
            byte[] body = decode(restPackage);
            response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, getContentType()));
            response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
            response.setBody(body);
        }
        return response;
    }

    protected abstract String getContentType();

    protected O get(O restPackage) {
        throw new UnsupportedOperationException("GET method is not implemented on the REST interface");
    }

    protected O post(O restPackage) {
        throw new UnsupportedOperationException("POST method is not implemented on the REST interface");
    }

    protected O put(O restPackage) {
        throw new UnsupportedOperationException("PUT method is not implemented on the REST interface");
    }

    protected O delete(O restPackage) {
        throw new UnsupportedOperationException("DELETE method is not implemented on the REST interface");
    }
}
