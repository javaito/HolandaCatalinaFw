package org.hcjf.io.net.http;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class ContextTest {

    static Map<String, HttpServer.AccessControl> accessControlMap;


    @Test
    public void testOnOptions() throws MalformedURLException {
        accessControlMap = new HashMap<>();
        String keyHeader = "http://localhost:5000";
        String key = "localhost";
        HttpServer.AccessControl accessControl = new HttpServer.AccessControl(key);
        accessControlMap.put(key, accessControl);
        HttpHeader httpHeader = new HttpHeader("ORIGIN", keyHeader);
        Context context = new Context("") {
            @Override
            public HttpResponse onContext(HttpRequest request) {
                return null;
            }
        };

        HttpResponse response = context.onOptions(httpHeader, accessControlMap);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpResponseCode.OK, response.getResponseCode());
        Assert.assertEquals(1, response.getHeaders().size());
    }
}
