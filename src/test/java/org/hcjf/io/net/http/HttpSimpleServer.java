package org.hcjf.io.net.http;

import org.hcjf.properties.SystemProperties;

public class HttpSimpleServer {

    public static void main(String[] args) {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.LEVEL, "2");
        System.setProperty(SystemProperties.Net.Ssl.DEFAULT_KEYSTORE_FILE_PATH,
                "/home/javaito/Git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/keystore.jks");
        System.setProperty(SystemProperties.Net.Ssl.DEFAULT_TRUSTED_CERTS_FILE_PATH,
                "/home/javaito/Git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/cacerts.jks");

        HttpResponse response = new HttpResponse();
        response.setBody("Hello world".getBytes());
        HttpsServer.create(9090, new Context(".*") {
            @Override
            public HttpResponse onContext(HttpRequest httpRequest) {
                System.out.println(httpRequest);
                return response;
            }
        });

//        HttpServer.create(8080, new Context(".*") {
//            @Override
//            public HttpResponse onContext(HttpRequest httpRequest) {
//                return response;
//            }
//        });
    }

}
