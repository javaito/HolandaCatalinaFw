package org.hcjf.io.net.http;

import org.hcjf.properties.SystemProperties;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ssl.SSLParameters;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author javaito.
 */
public class HttpEchoTest {

    //@BeforeClass
    public static void startEchoServer() {
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
                    byte[] body = request.getBody();

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

    //@Test
    public void echoTest() {
        List<TestThread> threadList = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            threadList.add(new TestThread(1));
        }

        threadList.forEach(T -> T.start());
        threadList.forEach(T -> {
            try {
                T.join();
            } catch (InterruptedException e) {
            }
        });
    }

    public static void main(String[] args) throws Exception {
        HttpClient httpClient = new HttpClient(new URL("http://beta.sitrack.io/edna?q=SELECT * FROM lipigas.Order"));
        httpClient.setHttpMethod(HttpMethod.GET);
        httpClient.setHttpsInsecureConnection(true);
        HttpResponse response = httpClient.request();

        System.out.println(response);
    }

    private static class TestThread extends Thread {

        private final Random random;
        private final Integer requestAmount;

        public TestThread(Integer requestAmount) {
            this.requestAmount = requestAmount;
            this.random = new Random();
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < requestAmount; i++) {
                    byte[] body = "Hello world!".getBytes();
                    HttpClient client = new HttpClient(new URL("http://localhost:8081/"));
                    client.setHttpMethod(HttpMethod.GET);
                    client.setBody(body);
                    client.addHttpHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, "text/plain"));
                    client.addHttpHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
                    HttpResponse response = client.request();
                    Assert.assertEquals(new String(body), new String(response.getBody()));
                    Thread.sleep((long) (random.nextDouble() * 100));
                }
            } catch (Exception ex){}
        }
    }
}
