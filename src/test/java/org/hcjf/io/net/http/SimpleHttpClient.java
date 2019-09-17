package org.hcjf.io.net.http;

import java.net.URL;

public class SimpleHttpClient {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000; i++) {
            try {
                HttpClient client = new HttpClient(new URL("https://localhost:6065"));
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse response = client.request();
                System.out.println(new String(response.getBody()));
            } catch (Exception ex){
                ex.printStackTrace();
            }
            //Thread.sleep(10000);
        }
    }

}
