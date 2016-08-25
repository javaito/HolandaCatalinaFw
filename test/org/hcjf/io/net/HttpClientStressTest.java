package org.hcjf.io.net;

import org.hcjf.io.net.http.HttpClient;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.HttpResponseCode;
import org.hcjf.log.Log;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

/**
 * Created by javaito on 25/08/16.
 */
public class HttpClientStressTest {

    @Test(timeout = 30000)
    public void test() throws Exception {
        HttpClient client = new HttpClient(new URL("http://rest-service.guides.spring.io/greeting"));
        long startTime;
        HttpResponse response;
        for (int i = 0; i < 100; i++) {
            startTime = System.currentTimeMillis();
            response = client.request();
            Log.d("TEST", "Http client stress %d", (System.currentTimeMillis() - startTime));
            Assert.assertEquals(response.getResponseCode(), HttpResponseCode.OK);
            client.reset();
        }
    }

}
