package org.hcjf.utils;

import org.hcjf.bson.BsonDocument;
import org.hcjf.utils.bson.BsonParcelable;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        TestSerializable testSerializable = new TestSerializable();
        testSerializable.setField1("Hello world!!");
        testSerializable.setField2(37);

        TestClass testObject = new TestClass();
        testObject.setMap1(map1);
        testObject.setMap2(map2);
        testObject.setMap3(map3);
        testObject.setTestSerializable(testSerializable);

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

    public static class TestClass implements BsonParcelable {

        private Map<UUID, Integer> map1;
        private Map<String, Double> map2;
        private Map<UUID, Map<String, Date>> map3;
        private TestSerializable testSerializable;

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
}
