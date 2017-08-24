package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.io.net.InetPortProvider;
import org.hcjf.io.net.http.pipeline.ChunkedHttpPipelineResponse;
import org.hcjf.io.net.http.rest.EndPoint;
import org.hcjf.io.net.http.rest.layers.EndPointDecoderLayerInterface;
import org.hcjf.io.net.http.rest.layers.EndPointEncoderLayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.CrudLayer;
import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.layers.crud.IdentifiableLayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.grants.Grant;
import org.hcjf.utils.Introspection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public class HttpServerTestSuit {

    public static void main(String[] args) {

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.TRUNCATE_TAG, "true");
        System.setProperty(SystemProperties.Net.Http.DEFAULT_CLIENT_READ_TIMEOUT, "60000");
//        System.setProperty(SystemProperties.Net.Http.OUTPUT_LOG_BODY_MAX_LENGTH, Integer.toString(Integer.MAX_VALUE));
//        System.setProperty(SystemProperties.Net.IO_THREAD_POOL_MAX_SIZE, Integer.toString(Integer.MAX_VALUE));

        Layers.publishLayer(EndPointDecoderLayerInterface.JsonEndPointDecoder.class);
        Layers.publishLayer(EndPointEncoderLayerInterface.JsonEndPointEncoder.class);
        Layers.publishLayer(TestCrud.class);
        Layers.publishLayer(Test1Crud.class);
        Layers.publishLayer(TestMapCrud.class);

//        for (int i = 0; i < 1; i++) {
//            try {
//                HttpClient client = new HttpClient(new URL("http://www.httpwatch.com/httpgallery/chunked/chunkedimage.aspx"));
//                HttpResponse response = client.request();
//                System.out.println(response);
//
//                FileOutputStream fileOutputStream = new FileOutputStream(new File("/home/javaito/chunked.jpeg"));
//                fileOutputStream.write(response.getBody());
//                fileOutputStream.flush();
//            } catch (Exception ex){
//                ex.printStackTrace();
//            }
//        }

//        try {
//            HttpServer server = new HttpServer(9091);
//            server.addContext(new Context("/test.*") {
//                @Override
//                public HttpResponse onContext(HttpRequest request) {
//                    HttpResponse response = new ChunkedHttpPipelineResponse(32) {
//
//                        private FileInputStream fileInputStream;
//
//                        @Override
//                        public void onStart() {
//                            try {
//                                fileInputStream = new FileInputStream(new File("/home/javaito/chunked.jpeg"));
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onEnd() {
//                            try {
//                                fileInputStream.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        protected int readPipeline(byte[] buffer) {
//                            try {
//                                return fileInputStream.read(buffer);
//                            } catch (IOException e) {
//                                throw new RuntimeException();
//                            }
//                        }
//                    };
//
////                    HttpResponse response = new HttpResponse();
//                    response.setResponseCode(200);
//                    response.setReasonPhrase("OK");
//                    response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, "image/jpeg; charset=utf-8"));
//                    response.addHeader(new HttpHeader("Cache-Control", "no-cache, no-store"));
//                    response.addHeader(new HttpHeader("Expires", "-1"));
////                    try {
////                        response.setBody(Files.readAllBytes(Paths.get("/home/javaito/chunked.jpeg")));
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
//                    return response;
//                }
//            });
//            server.start();
//        } catch (Exception ex){}
//
//        Log.d("CHAU", "End");
//
//        HttpServer server = new HttpServer(8080);
//        server.addContext(new FolderContext("", Paths.get("/home/javaito/Imágenes/test")) {
//
//            @Override
//            public HttpResponse onContext(HttpRequest request) {
//                HttpResponse response = super.onContext(request);
//                response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.JPG));
//                return response;
//            }
//        });
//        server.start();

        ///git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https
//        HttpsServer server = new HttpsServer(8443);
//        server.setKeystoreFilePath(Paths.get("/home/javaito/git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/keystore.jks"));
//        server.setTrustedCertsFilePath(Paths.get("/home/javaito/git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/cacerts.jks"));
//        server.addContext(new EndPoint("example", "crud"));
//        server.addContext(new Context(".*") {
//            @Override
//            public HttpResponse onContext(HttpRequest request) {
//                System.out.println(request);
//                throw new RuntimeException();
//            }
//        });
//        server.start();

//        try {
//            HttpClient client = new HttpClient(new URL("https://localhost:8443/"));
//            client.addHttpHeader("Accept: text/html");
//            client.addHttpHeader("User-Agent: HCJF");
//            client.addHttpHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
//            HttpResponse response = client.request();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }

//        try {
//            HttpClient client = new HttpClient(new URL("https://downstream.ypf.com/pls/extra/Pkg_Ip_General.PRC_DETALLE_TRANSAN_EXCEL?p_fecha_desde=01/04/2017&p_fecha_hasta=01/04/2017&p_fecha_desde_dT=&p_fecha_hasta_dT=&p_fecha_desde_d=&p_fecha_hasta_d=&p_contrato=112559&p_cuenta=&p_patente=&p_id_estacion=&p_id_producto=&p_orden=&p_orderby=&p_nro_factura=&p_id_producto_res="));
//            client.addHttpHeader(new HttpHeader("Cookie", "TS010b4846=014224af285826071fb9a9c592b3ad632f9118fff86ba345b99e5341407c599b88baf382908214309d93d65db6d25bf120e7903c81fc3ef10fe4caa17cb68e69201d47d8bccc44ac20db6533906dca4c7d8a87d861683cdbd8e9c8a6110a7ad5c0e19af30d; ORA_WX_SESSION=AECECA30A434965855B0D45AFA382376B560FADC-6#4; YESESSION=tt9a8fB7IS/YbFZi+xHN2dQWkyz9NQxy9zYfDZWfYH1/l5DWBLeDnssGLcRT62Q7o5aKXZff2d8+u4ulSucoBWg2PtF8RAFlraTaWWOh4RYl2ax8PTM1N6D+0SHsFd6zRAtWaXOBTWtNLU33r76rn9Gi3quUFJklShmJd5e7p1KXR2QZrtU9fy4+z9hEqxkCp/+Rbcs4NHFdCMbPDn/fdznbi5lq+DUZjwExOsFLnKLjRoJfcNcAL0gwJGClzu6XY05SOm25ha/Qti3eMga+osJYMKfZzPZuAAq5xhwrhStSDoaZtGNkge0egHISz/BYfICIaSYuAXn1lR9yHDqjJ2TQxJqt+Inj0I+nI/Uy6MACM5ruGzZTFpr/De9QWAII27JKmvnLvLoSKMM71EG9Y23oqO3lLMBf6SpjJt2qTCOd8SAJnk+3SwJw9qs5mBC87ScVWi2ikg9YR4S4kYUN3yKxMNoF+KtvSpUMRgqgQu9LBoIvQ7ds2wSP2LDfWUPWfD//lhSIBitP4XtSvNWytoa0sGZgA+aqRefI51ssGz2i+rAL5h+NDYSO17X5UZ59a9wawsq5tF8iCPmzTQcGyrJUxtd9aPnEc6DEoKY92hyUfCiSDsXepS2Gvq8QTXB7Z9eQErBSYw74wKeTDjrJT/BN2pTmMkA/OmcA4jLjjhrE7atMnWeaqROjH3NyR1c/plslD57T57dFNNxyzZiVaKTVele+BnGLRZqW81REeXeEd5HbX51YJuP8p2lc4SLo09IlhE3hagnNPu4JVXsJl6Teg0VukRDraxH/KHelIs7Cr/7PDdXSsk53nEaeKLGfqLOS/ucrh93heaJA1dld4T5r83fFBGtIzIFbbnjtEPnlJiXNhN7IrZzne8aJLN8GZbWcZ/54FlVPOsdI/5SYvp4USd6OkowUBJLsTBFvAc7jeP7Du3McqS2dL89U3GnuHuNbwM0SD7kFZFOM9ZaU1p6opOnE5nko5wj9B8iCIj8tnA5FpZhl2dLk4tQ+FwHnuGdZvuuMgLTbYifpcCHMkUfkhbr8i1lAQ85VAAwr9/r9Pwtm5YGz8zPWUCVjjmjuqQ6+k6mgEW1H1auhEuCzNk9Gm/sPL9khvzWR8HOp8+dzSEmCjdhFS+MkiHFRvjarPSd8ab6aNqXRTxXOytKBAKLb8iqOW42B3b39UD7Ow30gNbJS0HYaz0sZ8JXE2M2+J4q+aR2NCaZ/biEMTRcXtSyUzHT5qWaWUIjFmGMo4O4="));
////            client.addHttpHeader("Accept-Encoding: gzip,deflate");
//            client.addHttpHeader("Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
//            client.addHttpHeader("User-Agent: Java1.8.0_131");
//            client.addHttpHeader("Pragma: no-cache");
//            client.addHttpHeader("Cache-Control: no-cache");
//            client.addHttpHeader("Connection: keep-alive");
//            HttpResponse response = client.request();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            HttpClient client = new HttpClient(new URL("https://www.amazon.com"));
//            client.addHttpHeader("Accept-Encoding: gzip,deflate");
//            client.addHttpHeader("User-Agent: HCJF");
//            client.addHttpHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
//            HttpResponse response = client.request();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            HttpClient client = new HttpClient(new URL("https://www.google.com.ar/?gfe_rd=cr&ei=T6EkWYu0CpCq8wfG0aXgAg"));
//            client.addHttpHeader("Accept: text/html");
//            client.addHttpHeader("User-Agent: HCJF");
//            client.addHttpHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
//            HttpResponse response = client.request();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }

        HttpServer server = new HttpServer(InetPortProvider.getTcpPort(8080));
        server.addContext(new EndPoint("example", "crud"));
        server.start();

//        HttpsServer server = new HttpsServer(8080);
//        server.addContext(new EndPoint("example", "crud"));
//        server.setKeystoreFilePath(Paths.get("/home/javaito/git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/keystore.jks"));
//        server.setTrustedCertsFilePath(Paths.get("/home/javaito/git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/cacerts.jks"));
//        server.start();

//        try {
//            HttpServer server = new HttpServer(8080);
//            FolderContext folderContext = new FolderContext("",
//                    Paths.get("/home/javaito/git/HolandaCatalinaFw/out/artifacts/hcjf_jar/hcjf.jar"));
//            server.addContext(folderContext);
//            server.start();
//        } catch (Exception ex){
//            ex.printStackTrace();
//        }
    }

    public static class TestMapCrud extends CrudLayer<Map<String, Object>> {

        private final Grant createGrant;
        private final Grant customGrant;

        public TestMapCrud() {
            createGrant = Grant.publishGrant("CREATE");
            customGrant = Grant.publishGrant("CACA");
        }

        @Override
        public String getImplName() {
            Method m= getClass().getEnclosingMethod();
            return "Test2";
        }

        @Override
        public Collection<Map<String, Object>> read(Query query) {

            Grant.validateGrant(createGrant);

            Collection<Map<String, Object>> result = new ArrayList<>();
            Collection<Test> tests = Layers.get(CrudLayerInterface.class, "Test").read(query);
            for(Test test : tests) {
                Map<String, Object> testMap = Introspection.toMap(test);
                testMap.put("field", "fieldValue");
                result.add(testMap);
            }
            return result;
        }

        @Override
        public Collection<JoinableMap> readRows(Query query) {
            Collection<JoinableMap> result = new ArrayList<>();
            Collection<Test> tests = Layers.get(CrudLayerInterface.class, "Test").read(query);
            for(Test test : tests) {
                Map<String, Object> testMap = Introspection.toMap(test);
                testMap.put("field", "fieldValue");
                result.add(new JoinableMap(testMap));
            }
            return result;
        }
    }

    public static class TestCrud extends CrudLayer<Test> implements IdentifiableLayerInterface<Test> {

        private final Map<UUID, Test> dataBase;

        public TestCrud() {
            this.dataBase = new HashMap<>();
        }

        @Override
        public String getImplName() {
            return "Test";
        }

        @Override
        public Test create(Test object) {
            object.setId(createId());
            dataBase.put(object.getId(), object);
            return object;
        }

        @Override
        public Test read(Object id) {
            UUID uuid;
            if(id instanceof String) {
                uuid = UUID.fromString ((String)id);
            } else if(id instanceof UUID) {
                uuid = (UUID) id;
            } else {
                throw new RuntimeException("");
            }

            return dataBase.get(uuid);
        }

        @Override
        public Test update(Test object) {
            return dataBase.put(object.getId(), object);
        }

        @Override
        public Test delete(Object id) {
            UUID uuid;
            if(id instanceof String) {
                uuid = UUID.fromString ((String)id);
            } else if(id instanceof UUID) {
                uuid = (UUID) id;
            } else {
                throw new RuntimeException("");
            }

            return dataBase.remove(uuid);
        }

        @Override
        public Collection<Test> read(Query query) {
            return query.evaluate(dataBase.values());
        }

        @Override
        public Collection<JoinableMap> readRows(Query query) {
            Collection<JoinableMap> result = new ArrayList<>();
            dataBase.values().forEach(T -> result.add(new JoinableMap(Introspection.toMap(T))));
            return result;
        }
    }

    public static class Test1Crud extends CrudLayer<Test1> implements IdentifiableLayerInterface<Test1> {

        private final Map<UUID, Test1> dataBase;

        public Test1Crud() {
            this.dataBase = new HashMap<>();
        }

        @Override
        public String getImplName() {
            return "Test1";
        }

        @Override
        public Test1 create(Test1 object) {
            CrudLayerInterface<Test> crudLayerInterface = Layers.get(CrudLayerInterface.class, "Test");
            Test test = crudLayerInterface.read(object.getTestId());
            if(test == null) {
                throw new IllegalArgumentException();
            }

            object.setId(createId());
            dataBase.put(object.getId(), object);
            return object;
        }

        @Override
        public Test1 read(Object id) {
            UUID uuid;
            if(id instanceof String) {
                uuid = UUID.fromString ((String)id);
            } else if(id instanceof UUID) {
                uuid = (UUID) id;
            } else {
                throw new RuntimeException("");
            }

            return dataBase.get(uuid);
        }

        @Override
        public Test1 delete(Object id) {
            UUID uuid;
            if(id instanceof String) {
                uuid = UUID.fromString ((String)id);
            } else if(id instanceof UUID) {
                uuid = (UUID) id;
            } else {
                throw new RuntimeException("");
            }

            return dataBase.remove(uuid);
        }

        @Override
        public Collection<Test1> read(Query query) {
            return query.evaluate(dataBase.values());
        }

        @Override
        public Collection<JoinableMap> readRows(Query query) {
            Collection<JoinableMap> result = new ArrayList<>();
            dataBase.values().forEach(T -> result.add(new JoinableMap(Introspection.toMap(T))));
            return result;
        }
    }

    public static class Test {

        private UUID id;
        private String name;
        private Integer count;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    public static class Test1 {

        private UUID id;
        private String name1;
        private Integer count1;
        private UUID testId;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName1() {
            return name1;
        }

        public void setName1(String name1) {
            this.name1 = name1;
        }

        public Integer getCount1() {
            return count1;
        }

        public void setCount1(Integer count1) {
            this.count1 = count1;
        }

        public UUID getTestId() {
            return testId;
        }

        public void setTestId(UUID testId) {
            this.testId = testId;
        }
    }
}
