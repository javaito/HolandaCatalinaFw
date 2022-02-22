package org.hcjf.io.net.http;

import org.hcjf.properties.SystemProperties;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SimpleHttpClient {

//    @Test
    public void testHttpClient() {
        for (int i = 0; i < 20; i++) {
            try {
                HttpClient client = new HttpClient(new URL("https://apis.digital.gob.cl/fl/feriados/2021"));
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

//    @Test
    public void testHttpClient2() {
        for (int i = 0; i < 20; i++) {
            try {
                HttpClient client = new HttpClient(new URL("https://apis.digital.gob.cl/fl/feriados/2021"));
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

//    @Test
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

//    @Test
    public void testHttpsAsyncClientImage() {

//        String url = "https://www.google.com.ar/";
//        String url = "https://www.example.com/";
        String url = "http://storage.googleapis.com/beta-gdalcalcout-0000001f-0494-45f1-0000-002e52e279b1//Gdal/calc/gdalCalctest.tif";

        for (int i = 0; i < 1; i++) {
            try {
                HttpClient client = new HttpClient(new URL(url));
                client.setHttpMethod(HttpMethod.GET);
                HttpResponse callback = client.asyncRequest(new HttpResponseHandler() {
                    @Override
                    protected void consume(ByteBuffer fragment) {
                        System.out.println("Datos:" + new String(fragment.array()));
                        System.out.println("Length: " + fragment.limit());
                        /*
                        try {
                            Files.write(Path.of("/home/javaito/Descargas/image2.png"), fragment.array(), StandardOpenOption.CREATE_NEW);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                         */
                    }
                });
                ((HttpResponseHandler)callback.getTransferDecodingLayer()).get();
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
}
