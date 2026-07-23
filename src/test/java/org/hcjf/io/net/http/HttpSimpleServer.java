package org.hcjf.io.net.http;

import org.hcjf.io.net.http.ws.WebSocketContext;
import org.hcjf.properties.SystemProperties;

public class HttpSimpleServer {

    public static void main(String[] args) {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.LEVEL, "1");
        System.setProperty(SystemProperties.Layer.PUBLISH_EVALUATOR_IMPLEMENTATIONS, "false");
        System.setProperty(SystemProperties.Net.Ssl.DEFAULT_KEYSTORE_FILE_PATH,
                "/home/javaito/Git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/keystore.jks");
        System.setProperty(SystemProperties.Net.Ssl.DEFAULT_TRUSTED_CERTS_FILE_PATH,
                "/home/javaito/Git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/cacerts.jks");

        HttpResponse response = new HttpResponse();
        response.setResponseCode(200);
        response.setBody("Hello world".getBytes());
        HttpServer.create(9090, new Context("/test") {
            @Override
            public HttpResponse onContext(HttpRequest httpRequest) {
                return response;
            }
        }, new WebSocketContext("/ws/events") {
            @Override
            public void onOpen(HttpSession session) {

            }

            @Override
            public void onMessage(HttpSession session, String message) {
                sendText(session, "Echo: " + message);
            }

            @Override
            public void onClose(HttpSession session) {

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
