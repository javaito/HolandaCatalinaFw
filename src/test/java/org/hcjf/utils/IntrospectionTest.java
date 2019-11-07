package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author javaito
 */
public class IntrospectionTest {

    @Test
    public void testGetters() {
        try {
            Map<String, Introspection.Getter> getters = Introspection.getGetters(TestEntity.class);
            Assert.assertTrue(getters.containsKey("integer"));
            Assert.assertTrue(getters.get("integer").getReturnType().equals(Integer.class));
            Assert.assertTrue(getters.containsKey("map1"));
            Assert.assertTrue(getters.get("map1").getReturnType().equals(Map.class));
            Assert.assertTrue(getters.get("map1").getReturnKeyType().equals(String.class));
            Assert.assertTrue(getters.get("map1").getReturnCollectionType().equals(String.class));
            Assert.assertTrue(getters.containsKey("map2"));
            Assert.assertTrue(getters.get("map2").getReturnType().equals(Map.class));
            Assert.assertTrue(getters.get("map2").getReturnKeyType().equals(String.class));
            Assert.assertTrue(getters.get("map2").getReturnCollectionType().equals(Set.class));
            Assert.assertTrue(getters.containsKey("collection1"));
            Assert.assertTrue(getters.get("collection1").getReturnType().equals(Collection.class));
            Assert.assertTrue(getters.get("collection1").getReturnKeyType() == null);
            Assert.assertTrue(getters.get("collection1").getReturnCollectionType().equals(String.class));
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testSetters() {
        try {
            Map<String, Introspection.Setter> setters = Introspection.getSetters(TestEntity.class);
            Assert.assertTrue(setters.containsKey("integer"));
            Assert.assertTrue(setters.get("integer").getParameterType().equals(Integer.class));
            Assert.assertTrue(setters.containsKey("map1"));
            Assert.assertTrue(setters.get("map1").getParameterType().equals(Map.class));
            Assert.assertTrue(setters.get("map1").getParameterKeyType().equals(String.class));
            Assert.assertTrue(setters.get("map1").getParameterCollectionType().equals(String.class));
            Assert.assertTrue(setters.containsKey("map2"));
            Assert.assertTrue(setters.get("map2").getParameterType().equals(Map.class));
            Assert.assertTrue(setters.get("map2").getParameterKeyType().equals(String.class));
            Assert.assertTrue(setters.get("map2").getParameterCollectionType().equals(Set.class));
            Assert.assertTrue(setters.containsKey("collection1"));
            Assert.assertTrue(setters.get("collection1").getParameterCollectionType().equals(String.class));
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testToInstance() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("integer", 67);
            map.put("decimal", 0.6);
            TestEntity entity = Introspection.toInstance(map, TestEntity.class);
            Assert.assertNotNull(entity);
            Assert.assertNotEquals(0.0, entity.getDecimal());
            Assert.assertNotEquals(Integer.valueOf(0), entity.getInteger());
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testSet() {
        try {
            NestedTestEntity entity = new NestedTestEntity();
            Introspection.set(entity, "entity", new TestEntity());
            Introspection.set(entity, "entity.integer", 3);
            Introspection.set(entity, "entity.decimal", 3.5);
            Introspection.set(entity, "entity.map1", new HashMap<>(Map.of("name", "nombre", "key", "clave")));
            Introspection.set(entity, "entity.map1.id", "23456");

            Assert.assertEquals(entity.getEntity().getInteger(), Integer.valueOf(3));
            Assert.assertEquals(entity.getEntity().getDecimal(), Double.valueOf(3.5));
            Assert.assertEquals(entity.getEntity().getMap1().get("id"), "23456");
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    private static class NestedTestEntity {
        private TestEntity entity;

        public TestEntity getEntity() {
            return entity;
        }

        public void setEntity(TestEntity entity) {
            this.entity = entity;
        }
    }

    private static class TestEntity extends InheritanceTestEntity<String, Integer> {

        private Integer integer;
        private Map<String, String> map1;
        private Map<String, Set<String>> map2;
        private Collection<String> collection1;
        private Double decimal;

        public TestEntity() {
        }

        public Integer getInteger() {
            return integer;
        }

        public void setInteger(Integer integer) {
            this.integer = integer;
        }

        public Map<String, String> getMap1() {
            return map1;
        }

        public void setMap1(Map<String, String> map1) {
            this.map1 = map1;
        }

        public Map<String, Set<String>> getMap2() {
            return map2;
        }

        public void setMap2(Map<String, Set<String>> map2) {
            this.map2 = map2;
        }

        public Collection<String> getCollection1() {
            return collection1;
        }

        public void setCollection1(Collection<String> collection1) {
            this.collection1 = collection1;
        }

        public Double getDecimal() {
            return decimal;
        }

        public void setDecimal(Double decimal) {
            this.decimal = decimal;
        }

        @Override
        public Map<String, Integer> getGenericMap() {
            return super.getGenericMap();
        }
    }

    private static class InheritanceTestEntity<K extends Object, V extends Object> {

        private Collection<V> genericCollection;
        private Map<K,V> genericMap;

        public Collection<V> getGenericCollection() {
            return genericCollection;
        }

        public void setGenericCollection(Collection<V> genericCollection) {
            this.genericCollection = genericCollection;
        }

        public Map<K, V> getGenericMap() {
            return genericMap;
        }

        public void setGenericMap(Map<K, V> genericMap) {
            this.genericMap = genericMap;
        }
    }
}
