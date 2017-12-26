package org.hcjf.io.net.http;

import org.hcjf.properties.SystemProperties;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public class Http2Test {

    public static void main(String[] args) {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.TRUNCATE_TAG, "true");
        System.setProperty(SystemProperties.Net.Http.DEFAULT_CLIENT_READ_TIMEOUT, "60000");
        System.setProperty(SystemProperties.Service.THREAD_POOL_CORE_SIZE, "100");
        System.setProperty(SystemProperties.Service.THREAD_POOL_MAX_SIZE, "2000");

        try {
            HttpServer server = new HttpServer(8081);
            server.addContext(new Context(".*") {
                @Override
                public HttpResponse onContext(HttpRequest request) {
                    byte[] body = "Hola mundo!".getBytes();

                    HttpResponse response = new HttpResponse();
                    response.setResponseCode(200);
                    response.setReasonPhrase("OK");
                    response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, "text/plain"));
                    response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
                    response.setBody(body);

                    return response;
                }
            });
            server.start();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
