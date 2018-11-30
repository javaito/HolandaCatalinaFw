package org.hcjf.io.net.http;

public class SimpleRestEndPoint {

    public static void main(String[] args) {
        HttpResponse response = new HttpResponse();
        response.setBody("Hello world".getBytes());
        HttpServer.create(9090, new RestContext("base"));
    }

}
