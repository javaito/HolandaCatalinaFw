package org.hcjf.utils;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

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

    public class Bean {
        public String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testBean() {
        Bean bean = new Bean();
        bean.setName("javier");

        System.out.println(Introspection.resolve(bean, "name").toString());
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

    @Test
    public void testWithListAsRoot() {
        List<Map<String,Object>> list = new ArrayList<>();
        list.add(Map.of("key", "value1"));
        list.add(Map.of("key", "value2"));
        System.out.println(Introspection.resolve(list, "**.key").toString());
    }

    @Test
    public void testWildCardCharacter() {
        Map<String,Object> body = new HashMap<>();
        Collection<Map<String,Object>> collection = new ArrayList<>();
        body.put("collection", collection);
        for (int i = 0; i < 10; i++) {
            Map<String,Object> mapIntoCollection = new HashMap<>();
            mapIntoCollection.put("i", i);
            mapIntoCollection.put("module", i%2);
            mapIntoCollection.put("moduleToString", Objects.toString(i%2));
            Map<String,Object> innerMap = new HashMap<>();
            innerMap.put("inner_i", i);
            innerMap.put("inner_module", i%2);
            innerMap.put("inner_moduleToString", Objects.toString(i%2));
            mapIntoCollection.put("innerMap", innerMap);
            Collection<Map<String,Object>> innerCollection = new ArrayList<>();
            mapIntoCollection.put("innerCollection", innerCollection);
            for (int j = 0; j < 10; j++) {
                Map<String,Object> j_mapIntoCollection = new HashMap<>();
                j_mapIntoCollection.put("j", j);
                j_mapIntoCollection.put("module", j%2);
                j_mapIntoCollection.put("moduleToString", Objects.toString(j%2));
                Map<String,Object> j_innerMap = new HashMap<>();
                j_innerMap.put("inner_j", j);
                j_innerMap.put("inner_module", j%2);
                j_innerMap.put("inner_moduleToString", Objects.toString(j%2));
                j_mapIntoCollection.put("innerMap", j_innerMap);
                innerCollection.add(j_mapIntoCollection);
            }
            collection.add(mapIntoCollection);
        }

        Collection<Collection> scc = new ArrayList<>();
        body.put("scc", collection);
        for (int j = 0; j < 10; j++) {
            Collection<Map<String, Object>> cc = new ArrayList<>();
            scc.add(cc);
            for (int i = 0; i < 10; i++) {
                Map<String, Object> mapIntoCollection = new HashMap<>();
                mapIntoCollection.put("i", i);
                mapIntoCollection.put("module", i % 2);
                mapIntoCollection.put("moduleToString", Objects.toString(i % 2));
                Map<String, Object> innerMap = new HashMap<>();
                innerMap.put("inner_i", i);
                innerMap.put("inner_module", i % 2);
                innerMap.put("inner_moduleToString", Objects.toString(i % 2));
                mapIntoCollection.put("innerMap", innerMap);
                cc.add(mapIntoCollection);
            }
        }

        Object o = Introspection.resolve(body, "planilla.factura.**|.cliente.nombre collection.**.module");
        System.out.println(o);

        Object o1 = Introspection.resolve(body, "collection.*.module");
        System.out.println(o1);

        Object o2 = Introspection.resolve(body, "collection.**.innerCollection.**.module");
        System.out.println(o2);

        Object o3 = Introspection.resolve(body, "collection.**|.innerCollection.**.module");
        System.out.println(o3);

        Object o4 = Introspection.resolve(body, "collection.*|.innerCollection.**.module");
        System.out.println(o4);

        Object o5 = Introspection.resolve(body, "collection.**.innerMap.inner_module");
        System.out.println(o5);

        Object o6 = Introspection.resolve(body, "scc.**.**.module");
        System.out.println(o6);

        Object o7 = Introspection.resolve(body, "scc.**|.**.module");
        System.out.println(o7);

        Object o8 = Introspection.resolve(body, ".");
        System.out.println(o8);
    }

    @Test
    public void testResolveAndPut() {
        Map<String,Object> body = new HashMap<>();
        Collection<Map<String,Object>> collection = new ArrayList<>();
        body.put("collection", collection);
        for (int i = 0; i < 10; i++) {
            Map<String,Object> mapIntoCollection = new HashMap<>();
            mapIntoCollection.put("i", i);
            mapIntoCollection.put("module", i%2);
            mapIntoCollection.put("moduleToString", Objects.toString(i%2));
            Map<String,Object> innerMap = new HashMap<>();
            innerMap.put("inner_i", i);
            innerMap.put("inner_module", i%2);
            innerMap.put("inner_moduleToString", Objects.toString(i%2));
            mapIntoCollection.put("innerMap", innerMap);
            Collection<Map<String,Object>> innerCollection = new ArrayList<>();
            mapIntoCollection.put("innerCollection", innerCollection);
            for (int j = 0; j < 10; j++) {
                Map<String,Object> j_mapIntoCollection = new HashMap<>();
                j_mapIntoCollection.put("j", j);
                j_mapIntoCollection.put("module", j%2);
                j_mapIntoCollection.put("moduleToString", Objects.toString(j%2));
                Map<String,Object> j_innerMap = new HashMap<>();
                j_innerMap.put("inner_j", j);
                j_innerMap.put("inner_module", j%2);
                j_innerMap.put("inner_moduleToString", Objects.toString(j%2));
                j_mapIntoCollection.put("innerMap", j_innerMap);
                innerCollection.add(j_mapIntoCollection);
            }
            collection.add(mapIntoCollection);
        }

        Collection<Collection> scc = new ArrayList<>();
        body.put("scc", collection);
        for (int j = 0; j < 10; j++) {
            Collection<Map<String, Object>> cc = new ArrayList<>();
            scc.add(cc);
            for (int i = 0; i < 10; i++) {
                Map<String, Object> mapIntoCollection = new HashMap<>();
                mapIntoCollection.put("i", i);
                mapIntoCollection.put("module", i % 2);
                mapIntoCollection.put("moduleToString", Objects.toString(i % 2));
                Map<String, Object> innerMap = new HashMap<>();
                innerMap.put("inner_i", i);
                innerMap.put("inner_module", i % 2);
                innerMap.put("inner_moduleToString", Objects.toString(i % 2));
                mapIntoCollection.put("innerMap", innerMap);
                cc.add(mapIntoCollection);
            }
        }

        Map<String,Object> copyBody = Introspection.deepCopy(body);

        Object o1 = Introspection.resolveAndPut(body, "collection.**.innerMap", "newKLey", "newValue");
        System.out.println(o1);

        Object o2 = Introspection.resolveAndPut(body, "collection.0.innerMap", "newKLey2", "newValue2");
        System.out.println(o2);

        Object obj = Introspection.resolve(body, "collection.0.innerCollection");

        Object o3 = Introspection.resolveAndAdd(body, "collection.0.innerCollection", Map.of("newKLey2", "newValue2"));
        System.out.println(03);

        Object o4 = Introspection.resolveAndAdd(body, "collection.**|.innerCollection", Map.of("newKLey2", "newValue2"));
        System.out.println(o4);
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
