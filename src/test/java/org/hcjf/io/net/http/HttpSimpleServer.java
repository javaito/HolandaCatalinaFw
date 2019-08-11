package org.hcjf.io.net.http;

public class HttpSimpleServer {

    public static void main(String[] args) {
        HttpResponse response = new HttpResponse();
        response.setBody("Hello world".getBytes());
        HttpServer.create(9090, new Context(".*") {
            @Override
            public HttpResponse onContext(HttpRequest httpRequest) {
                return response;
            }
        });

        HttpServer.create(8080, new Context(".*") {
            @Override
            public HttpResponse onContext(HttpRequest httpRequest) {
                return response;
            }
        });
    }

}
