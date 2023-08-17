package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SimpleHttpClient {


  //  @Test
    public void testHereTraffic() throws Exception {
//        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
//        System.setProperty(SystemProperties.Log.LEVEL, "1");

        String apiKey = "kyY1-iqt2fcp07vL7jEHY6B7SL8KL6sze9K6M4O0XMQ";
        double latitude = 52.525439;
        double longitude = 13.38727;
        int zoom = 8;

        double latRad = latitude * Math.PI / 180;
        double n = Math.pow(2.0, zoom);
        double xTile = n * ((longitude + 180.0) / 360.0);
        double yTile = n * (1.0-(Math.log(Math.tan(latRad) + 1.0/Math.cos(latRad)) / Math.PI)) / 2.0;

        System.out.println(xTile);
        System.out.println(yTile);

        String url = "https://data.traffic.hereapi.com/v7/flow?in=circle:%s,%s;r=%s&locationReferencing=shape&apiKey=%s";
        url = String.format(url, latitude, longitude, 1000, apiKey);

        HttpClient client = new HttpClient(new URL(url));
        client.setReadTimeout(50000L);
        client.setWriteTimeout(50000L);
        client.setHttpMethod(HttpMethod.GET);
        HttpResponse callback = client.request();
        System.out.println(callback);

        System.out.println(JsonUtils.toJsonTree(new String(callback.getBody())).toString());
    }

  //  @Test
    public void testDarwin() throws Exception {
        String url = "https://ramuh.syntropysystem.com/uaa/security/login";


        String json = "{\n" +
                "\"password\": \"ZqBd64^7b1d#\",\n" +
                "\"username\": \"yanguas-sitrack\"\n" +
                "}";

        byte[] body = json.getBytes();

        HttpClient client = new HttpClient(new URL(url));
        client.setReadTimeout(50000L);
        client.setWriteTimeout(50000L);
        client.setHttpMethod(HttpMethod.POST);
        client.setBody(body);
        client.addHttpHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
        client.addHttpHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
        HttpResponse callback = client.request();
        System.out.println(callback);

        System.out.println(JsonUtils.toJsonTree(new String(callback.getBody())).toString());
    }

    @Test
    public void testDataDog() throws Exception {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.LEVEL, "1");

        String url = "https://api.datadoghq.com/api/v2/logs/events/search";

        HttpClient client = new HttpClient(new URL(url));
        client.setReadTimeout(50000L);
        client.setWriteTimeout(50000L);
        client.addHttpHeader(new HttpHeader("DD-APPLICATION-KEY", "f6845793aedd2976d25136832263186a1259a9d5"));
        client.addHttpHeader(new HttpHeader("DD-API-KEY", "cf366afbb2985ad49ed5710186b28e16"));
        client.setHttpMethod(HttpMethod.POST);

        String body = "{\"filter\": {" +
                "\"from\": \"now-5d\"," +
                "\"to\": \"now\"," +
                "\"query\": \"triggerid:7ea018a3-09c6-4c2c-a875-fac3acfdca29\"" +
                "}}";

        byte[] bodyBytes = body.getBytes(Charset.defaultCharset());
        client.setBody(bodyBytes);
        client.addHttpHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
        client.addHttpHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(bodyBytes.length)));

        HttpResponse callback = client.request();

        System.out.println();
    }

    //@Test
    public void testHereService() throws Exception {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.LEVEL, "1");

        String url = "https://geocode.search.hereapi.com/v1/geocode" +
                "?q=2250+Alsina%2C+Godoy+Cruz+Mendoza" +
                "&limit=4" +
                "&apiKey=kyY1-iqt2fcp07vL7jEHY6B7SL8KL6sze9K6M4O0XMQ";

        HttpClient client = new HttpClient(new URL(url));
        client.setReadTimeout(50000L);
        client.setWriteTimeout(50000L);
        client.setHttpMethod(HttpMethod.GET);
        HttpResponse callback = client.request();
        //Assert.assertEquals(callback.getResponseCode().longValue(), 200L);
        System.out.println(callback);

        System.out.println(JsonUtils.toJsonTree(new String(callback.getBody())).toString());
    }

    //@Test
    public void testHttpClient() {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.LEVEL, "1");

        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=-33.38779&lon=-70.52915&units=metric&appid=023ae9df86450046fb344b38a0709efd";
        //String url = "https://www.waze.com/live-map/api/reverse-geocoding?lat=-33.38779&lng=-70.52915&geoEnv=row&radius=0.0005";

        for (int i = 0; i < 1; i++) {
            try {
                HttpClient client = new HttpClient(new URL(url));
                client.setReadTimeout(50000L);
                client.setWriteTimeout(50000L);
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse callback = client.request();
                //Assert.assertEquals(callback.getResponseCode().longValue(), 200L);
                System.out.println(callback);

                System.out.println(JsonUtils.toJsonTree(new String(callback.getBody())).toString());

                System.out.printf("Request %d: ok\r\n", i);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.printf("Request %d: fail\r\n", i);
                Assert.fail();
            }
        }
    }

    //@Test
    public void testHttpClient2() {
        for (int i = 0; i < 1; i++) {
            try {
                HttpClient client = new HttpClient(new URL("https://www.example.com/"));
                client.setHttpsInsecureConnection(true);
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse callback = client.request();
                Assert.assertEquals(callback.getResponseCode().longValue(), 200L);
                System.out.println(callback);
                System.out.printf("Request %d: ok\r\n", i);
            } catch (Exception ex){
                ex.printStackTrace();
                System.out.printf("Request %d: fail\r\n", i);
                Assert.fail();
            }
        }
    }

    //@Test
    public void testHttpsClient() {

//        String url = "https://www.google.com.ar/";
//        String url = "https://www.example.com/";
        String url = "https://placehold.it/120x120&text=image1";

        for (int i = 0; i < 1; i++) {
            try {
                HttpClient client = new HttpClient(new URL(url));
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse callback = client.request();
//                Assert.assertEquals(callback.getResponseCode().longValue(), 200L);
//                Files.write(Path.of("/home/javaito/Descargas/image1.png"), callback.getBody(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                System.out.println("Response:" + callback);
                System.out.printf("Request %d: ok\r\n", i);
            } catch (Exception ex){
                ex.printStackTrace();
                System.out.printf("Request %d: fail\r\n", i);
                Assert.fail();
            }
        }
    }

    //@Test
    public void testHttpsAsyncClientImage() {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");

//        String url = "https://www.google.com.ar/";
//        String url = "https://www.example.com/";
        String url = "http://cordova.apache.org/static/img/cordova_bot.png";

        for (int i = 0; i < 1; i++) {
            try {
                HttpClient client = new HttpClient(new URL(url));
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse callback = client.asyncRequest(new HttpResponseHandler() {
                    @Override
                    protected void consume(ByteBuffer fragment) {
//                        System.out.println("Datos:" + new String(fragment.array()));
                        System.out.println("Length: " + fragment.limit());
                        try {
                            Files.write(Path.of("/home/javaito/Descargas/image2.png"), fragment.array(), StandardOpenOption.CREATE_NEW);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                ((HttpResponseHandler)callback.getTransferDecodingLayer()).get();
                //                System.out.println("Response:" + callback);
                System.out.printf("Request %d: ok\r\n", i);
            } catch (Exception ex){
                ex.printStackTrace();
                System.out.printf("Request %d: fail\r\n", i);
                Assert.fail();
            }
        }
    }

    //@Test
    public void testHttpsAsyncClient() {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.LEVEL, "2");

        for (int i = 0; i < 1; i++) {
            try {
                HttpClient client = new HttpClient(new URL("https://scihub.copernicus.eu/dhus/odata/v1/Products('42fd07f5-111f-4308-be6e-759f1015ceae')/$value"));
                client.addHttpHeader(new HttpHeader(HttpHeader.AUTHORIZATION, "Basic bGVvdGFycXVpbmk6U2l0cmFjazIyNTA="));
//                HttpClient client = new HttpClient(new URL("https://www.example.com/"));
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse callback = client.asyncRequest(new HttpResponseHandler() {
                    @Override
                    protected void consume(ByteBuffer fragment) {
                        try {
                            System.out.println("Length: " + fragment.limit());
                            Files.write(Path.of("/home/javaito/Descargas/super.zip"), fragment.array(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                System.out.println(callback);
                ((HttpResponseHandler)callback.getTransferDecodingLayer()).get();
                Assert.assertEquals(callback.getResponseCode().longValue(), 200L);
                System.out.printf("Request %d: ok\r\n", i);
            } catch (Exception ex){
                ex.printStackTrace();
                System.out.printf("Request %d: fail\r\n", i);
                Assert.fail();
            }
        }
    }

//    @Test
    public void testApiHttps() {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.LEVEL, "2");

        for (int i = 0; i < 1; i++) {
            try {
                HttpClient client = new HttpClient(new URL("https://beta.sitrack.io/edna"));
                client.addHttpHeader(new HttpHeader(HttpHeader.AUTHORIZATION, "basic YWRtaW5AYWRtaW4="));
                client.addHttpHeader(new HttpHeader(HttpHeader.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36"));
//                HttpClient client = new HttpClient(new URL("https://www.example.com/"));
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse callback = client.request();
                System.out.println(callback);
                ((HttpResponseHandler)callback.getTransferDecodingLayer()).get();
                Assert.assertEquals(callback.getResponseCode().longValue(), 200L);
                System.out.printf("Request %d: ok\r\n", i);
            } catch (Exception ex){
                ex.printStackTrace();
                System.out.printf("Request %d: fail\r\n", i);
                Assert.fail();
            }
        }
    }

    //@Test
    public void testOauth() {
        try {
            HttpClient client = new HttpClient(new URL("https://api.touchapp.cl/ccuprod/oauth2/token?grant_type=client_credentials&client_id=m2qek40CbfzOpSwH7X7g836tCQ2U9dRP&client_secret=cDtMbry1OV0TSG2AbK6V51YgAKwVqUBa"));
            client.setHttpMethod(HttpMethod.POST);
            HttpResponse callback = client.request();
            //Assert.assertEquals(callback.getResponseCode().longValue(), 200L);
            System.out.println(callback);
        } catch (Exception ex){
            ex.printStackTrace();
            Assert.fail();
        }
    }
}
