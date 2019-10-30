package org.hcjf.io.net.http;

import org.hcjf.properties.SystemProperties;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

public class SimpleHttpClient {

//    @Test
    public void testHttpClient() {
        for (int i = 0; i < 10; i++) {
            try {
                HttpClient client = new HttpClient(new URL("http://www.example.com/"));
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse callback = client.request();
                Assert.assertEquals(callback.getResponseCode().longValue(), 200L);
                System.out.printf("Request %d: ok\r\n", i);
            } catch (Exception ex){
                ex.printStackTrace();
                System.out.printf("Request %d: fail\r\n", i);
                Assert.fail();
            }
        }

        System.exit(0);
    }

//    @Test
    public void testHttpsClient() {
        for (int i = 0; i < 10; i++) {
            try {
                HttpClient client = new HttpClient(new URL("https://www.example.com/"));
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse callback = client.request();
                Assert.assertEquals(callback.getResponseCode().longValue(), 200L);
                System.out.printf("Request %d: ok\r\n", i);
            } catch (Exception ex){
                ex.printStackTrace();
                System.out.printf("Request %d: fail\r\n", i);
                Assert.fail();
            }
        }

        System.exit(0);
    }
}
