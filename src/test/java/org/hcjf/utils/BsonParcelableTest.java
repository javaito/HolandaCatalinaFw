package org.hcjf.utils;

import org.hcjf.bson.BsonDocument;
import org.hcjf.utils.bson.BsonParcelable;
import org.junit.Assert;
import org.junit.Test;

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

        TestClass testMap = new TestClass();
        testMap.setMap1(map1);
        testMap.setMap2(map2);
        testMap.setMap3(map3);

        BsonDocument bsonDocument = testMap.toBson();

        TestClass unserializedTestMap = BsonParcelable.Builder.create(bsonDocument);

        Assert.assertEquals(testMap.getMap1().get(new UUID(0, 12)),
                unserializedTestMap.getMap1().get(new UUID(0, 12)));
        Assert.assertEquals(testMap.getMap1().get(new UUID(0, 13)),
                unserializedTestMap.getMap1().get(new UUID(0, 13)));
    }

    public static class TestClass implements BsonParcelable {

        private Map<UUID, Integer> map1;
        private Map<String, Double> map2;
        private Map<UUID, Map<String, Date>> map3;

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
    }
}
