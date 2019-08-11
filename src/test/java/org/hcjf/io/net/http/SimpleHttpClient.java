package org.hcjf.io.net.http;

import java.net.URL;

public class SimpleHttpClient {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 200; i++) {
            HttpClient client = new HttpClient(new URL("http://beta.sitrack.io/edna?q=SELECT%20*%20FROM%20Account"));
            client.setHttpMethod(HttpMethod.GET);
            HttpResponse response = client.request();
            System.out.println(new String(response.getBody()));
        }
    }

}
