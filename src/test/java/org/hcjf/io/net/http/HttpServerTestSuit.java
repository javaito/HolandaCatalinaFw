package org.hcjf.io.net.http;

import org.hcjf.io.net.http.rest.EndPoint;
import org.hcjf.io.net.http.rest.layers.EndPointDecoderLayerInterface;
import org.hcjf.io.net.http.rest.layers.EndPointEncoderLayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.CrudLayer;
import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
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

        Layers.publishLayer(EndPointDecoderLayerInterface.JsonEndPointDecoder.class);
        Layers.publishLayer(EndPointEncoderLayerInterface.JsonEndPointEncoder.class);
        Layers.publishLayer(TestCrud.class);
        Layers.publishLayer(Test1Crud.class);
        Layers.publishLayer(TestMapCrud.class);


        try {
            HttpClient client = new HttpClient(new URL("https://www.amazon.com"));
            HttpResponse response = client.request();
            System.out.println(response);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpServer server = new HttpServer(8080);
        server.addContext(new EndPoint("example", "crud"));
        server.start();
    }

    public static class TestMapCrud extends CrudLayer<Map<String, Object>> {

        @Override
        public String getImplName() {
            Method m= getClass().getEnclosingMethod();
            return "Test2";
        }

        @Override
        public Collection<Map<String, Object>> read(Query query) {
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

    public static class TestCrud extends CrudLayer<Test> {

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
            object.setId(UUID.randomUUID());
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

    public static class Test1Crud extends CrudLayer<Test1> {

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

            object.setId(UUID.randomUUID());
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
