package org.hcjf.utils;

import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.ParameterizedQuery;
import org.hcjf.layers.query.Query;
import org.hcjf.utils.bson.BsonParcelable;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;

/**
 * @author javaito
 */
public class BsonParcelableTest {

    @Test
    public void testMap() {
        Map<UUID, Integer> map1 = new HashMap<>();
        map1.put(new UUID(0, 12), 12);
        map1.put(new UUID(0, 13), 13);

        Map<String,Double> map2 = new HashMap<>();
        map2.put("First", 23.6);
        map2.put("Second", 34.5);

        Map<String, Date> innerMap = new HashMap<>();
        innerMap.put("Today", new Date());
        innerMap.put("Hoy", new Date());

        Map<UUID, Map<String, Date>> map3 = new HashMap<>();
        map3.put(UUID.randomUUID(), innerMap);

        Test2 t2 = new Test2();
        t2.setPaths(new ArrayList<>());
        t2.getPaths().add(new Path(null, null, new ArrayList<>()));
        t2.toBson();

        TestSerializable testSerializable = new TestSerializable();
        testSerializable.setField1("Hello world!!");
        testSerializable.setField2(37);

        TestClass testObject = new TestClass();
        testObject.setMap1(map1);
        testObject.setMap2(map2);
        testObject.setMap3(map3);
        testObject.setTestSerializable(testSerializable);
        testObject.setTest2s(new Test2[]{t2, t2});

        BsonDocument bsonDocument = testObject.toBson();

        TestClass unserializedTestObject = BsonParcelable.Builder.create(bsonDocument);

        Assert.assertEquals(testObject.getMap1().get(new UUID(0, 12)),
                unserializedTestObject.getMap1().get(new UUID(0, 12)));
        Assert.assertEquals(testObject.getMap1().get(new UUID(0, 13)),
                unserializedTestObject.getMap1().get(new UUID(0, 13)));
        Assert.assertEquals(testObject.getTestSerializable().getField1(),
                unserializedTestObject.getTestSerializable().getField1());
        Assert.assertEquals(testObject.getTestSerializable().getField2(),
                unserializedTestObject.getTestSerializable().getField2());
    }

    @Test
    public void testByteArray() {
        TestByteArray testByteArray = new TestByteArray();
        testByteArray.setArray("test array".getBytes());

        BsonDocument bsonDocument = testByteArray.toBson();
        byte[] bsonAsArray = BsonEncoder.encode(bsonDocument);

        bsonDocument = BsonDecoder.decode(bsonAsArray);

        TestByteArray testByteArray1 = BsonParcelable.Builder.create(bsonDocument);
        Assert.assertEquals(new String(testByteArray.getArray()), new String(testByteArray1.getArray()));
    }

    @Test
    public void testAutoCast() {
        Map<UUID, Integer> map1 = new HashMap<>();
        map1.put(new UUID(0, 12), 12);
        map1.put(new UUID(0, 13), 13);

        Map<String,Double> map2 = new HashMap<>();
        map2.put("First", 23.6);
        map2.put("Second", 34.5);

        Map<String, Date> innerMap = new HashMap<>();
        innerMap.put("Today", new Date());
        innerMap.put("Hoy", new Date());

        Map<UUID, Map<String, Date>> map3 = new HashMap<>();
        map3.put(UUID.randomUUID(), innerMap);

        Test2 t2 = new Test2();
        t2.setPaths(new ArrayList<>());
        t2.getPaths().add(new Path(null, null, new ArrayList<>()));
        t2.toBson();

        TestSerializable testSerializable = new TestSerializable();
        testSerializable.setField1("Hello world!!");
        testSerializable.setField2(37);

        TestClass testObject = new TestClass();
        testObject.setMap1(map1);
        testObject.setMap2(map2);
        testObject.setMap3(map3);
        testObject.setTestSerializable(testSerializable);
        testObject.setTest2s(new Test2[]{t2, t2});

        BsonDocument bsonDocument = testObject.toBson();
        bsonDocument.put(BsonParcelable.PARCELABLE_CLASS_NAME, "no.class");

        Map<String,Object> mapFromBsonParcelable = BsonParcelable.Builder.create(bsonDocument);
    }

    @Test
    public void testQuery() {
        Query query = Query.compile("SELECT * FROM resource WHERE field1 = ?");
        BsonDocument document = query.toBson();
        byte[] serializedQuery = BsonEncoder.encode(document);
        document = BsonDecoder.decode(serializedQuery);
        Query query1 = BsonParcelable.Builder.create(document);
        Assert.assertEquals(query.toString(), query1.toString());

        ParameterizedQuery parameterizedQuery = new ParameterizedQuery(query1);
        parameterizedQuery.add(34);
        document = parameterizedQuery.toBson();
        serializedQuery = BsonEncoder.encode(document);
        document = BsonDecoder.decode(serializedQuery);
        ParameterizedQuery parameterizedQuery1 = BsonParcelable.Builder.create(document);


        System.out.println();
    }

    @Test
    public void testJoinableMap() {
        JoinableMap joinableMap = new JoinableMap("resource");
        joinableMap.put("fieldString", "fieldString");
        joinableMap.put("fieldDate", new Date());

        BsonDocument document = joinableMap.toBson();
        JoinableMap joinableMap1 = BsonParcelable.Builder.create(document);

        Assert.assertEquals(joinableMap1.get("fieldString"), joinableMap.get("fieldString"));
        Assert.assertEquals(joinableMap1.get("fieldDate"), joinableMap.get("fieldDate"));
    }

    public static class TestByteArray implements BsonParcelable {

        private byte[] array;

        public byte[] getArray() {
            return array;
        }

        public void setArray(byte[] array) {
            this.array = array;
        }
    }

    public static class TestClass implements BsonParcelable {

        private Map<UUID, Integer> map1;
        private Map<String, Double> map2;
        private Map<UUID, Map<String, Date>> map3;
        private TestSerializable testSerializable;
        private Test2[] test2s;

        public Map<UUID, Integer> getMap1() {
            return map1;
        }

        public void setMap1(Map<UUID, Integer> map1) {
            this.map1 = map1;
        }

        public Map<String, Double> getMap2() {
            return map2;
        }

        public void setMap2(Map<String, Double> map2) {
            this.map2 = map2;
        }

        public Map<UUID, Map<String, Date>> getMap3() {
            return map3;
        }

        public void setMap3(Map<UUID, Map<String, Date>> map3) {
            this.map3 = map3;
        }

        public TestSerializable getTestSerializable() {
            return testSerializable;
        }

        public void setTestSerializable(TestSerializable testSerializable) {
            this.testSerializable = testSerializable;
        }

        public Test2[] getTest2s() {
            return test2s;
        }

        public void setTest2s(Test2[] test2s) {
            this.test2s = test2s;
        }
    }

    public static class TestSerializable implements Serializable {

        private String field1;
        private Integer field2;

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public Integer getField2() {
            return field2;
        }

        public void setField2(Integer field2) {
            this.field2 = field2;
        }
    }

    public static class Test2 implements BsonParcelable {

        private List<Path> paths;

        public List<Path> getPaths() {
            return paths;
        }

        public void setPaths(List<Path> paths) {
            this.paths = paths;
        }
    }

    public static final class Path implements BsonParcelable {

        private Object[] path;
        private Object value;
        private List<UUID> nodes;

        public Path() {
        }

        public Path(Object[] path) {
            this.path = path;
        }

        public Path(Object[] path, List<UUID> nodes) {
            this.path = path;
            this.nodes = nodes;
        }

        public Path(Object[] path, Object value) {
            this.path = path;
            this.value = value;
        }

        public Path(Object[] path, Object value, List<UUID> nodes) {
            this.path = path;
            this.value = value;
            this.nodes = nodes;
        }

        public Object[] getPath() {
            return path;
        }

        public void setPath(Object[] path) {
            this.path = path;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public List<UUID> getNodes() {
            return nodes;
        }

        public void setNodes(List<UUID> nodes) {
            this.nodes = nodes;
        }
    }
}
