package org.hcjf.io.net.http;

import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.*;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;
import org.hcjf.properties.SystemProperties;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RestContextTest {

    @BeforeClass
    public static void setup() {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        Layers.publishLayer(TestResource.class);

        HttpServer server = new HttpServer(10080);
        server.addContext(new RestContext(".*"));
        server.start();
    }

    public static void main(String[] args) {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        Layers.publishLayer(TestResource.class);

        HttpServer server = new HttpServer(10080);
        server.addContext(new RestContext("/rest"));
        server.start();
    }

    @Test
    public void test() {
        System.out.println("Hello world 11");
    }

    public static class TestResource extends Layer implements
            CreateLayerInterface<Map<String,Object>>,
            UpdateLayerInterface<Map<String,Object>>,
            DeleteLayerInterface<Map<String,Object>>,
            ReadLayerInterface<Map<String,Object>>,
            ReadRowsLayerInterface {

        @Override
        public String getImplName() {
            return "test";
        }

        @Override
        public Map<String, Object> create(Map<String, Object> object) {
            return object;
        }

        @Override
        public Collection<Map<String, Object>> update(Queryable queryable, Map<String, Object> object) {
            return List.of();
        }

        @Override
        public Map<String, Object> update(Map<String, Object> object) {
            return object;
        }

        @Override
        public Map<String, Object> delete(Object id) {
            return null;
        }

        @Override
        public Collection<Map<String, Object>> delete(Queryable queryable) {
            return null;
        }

        @Override
        public Collection<Map<String, Object>> read() {
            return null;
        }

        @Override
        public Collection<Map<String, Object>> read(Queryable queryable) {
            return null;
        }

        @Override
        public Map<String, Object> read(Object id) {
            return null;
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            return new ArrayList<>();
        }
    }
}
