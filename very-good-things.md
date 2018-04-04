# Very good things!

In this section I want to show some of the most interesting things that are part of the internal structure of the framework, some of these things provide performance and scalability to the construction and maintenance of the project

## Table of Contents
 - [Internal services](##internal-services)
 - [Layers](##layers)
 - [Introspection cache](##introspection-cache)
 - [Queries](#queries)
 - [Bson Parcelable](##bson-parcelable)

## Internal services

An internal service is an internal component of the framework responsible for performing a specific task that maintains communication with the application layer through observers that provide an interface with the service. This type of services start to work on demand, if they are not used they do not add load to the framework instance
To create a new internal service you just have to write two classes one must implement the [ServiceConsumer](https://github.com/javaito/HolandaCatalinaFw/blob/master/src/main/java/org/hcjf/service/ServiceConsumer.java) interface and the other class that needs to be written has to extend the class [Service](https://github.com/javaito/HolandaCatalinaFw/blob/master/src/main/java/org/hcjf/service/Service.java).

### My service consumer class
``` java
public class MyServiceConsumer implements ServiceConsumer {
    //Write all the methods that the consumer need to implements the service interface
}
```

### My service class
``` java
public class MyService extends Service<MyServiceConsumer> {

    protected MyService(String serviceName, Integer priority) {
        super(serviceName, priority);
    }

    @Override
    protected void init() {
        //Write the code in order to config the service and start all the task of the service
    }

    @Override
    protected void shutdown(ShutdownStage stage) {
        //Write the code to ensure the shutdown of the service.
    }

    @Override
    public void registerConsumer(MyServiceConsumer consumer) {
        //Write the code to register a new instance of service consumer.
    }

    @Override
    public void unregisterConsumer(MyServiceConsumer consumer) {
        //Write the code to unregister the consumer.
    }
}
```

## Layers
The components of the type 'Layer' represent a generalization of the strategy pattern masking different implementations of the same task accessible through a generic interface.
All the layers must comply with three characteristics in order to be published:

 - It must have a name that identifies the implementation
 - It must specify whether they are of the stateful or stateless type
 - It must implement one or more interfaces which extend the [LayerInterface](org.hcjf.layers.LayerInterface.java) interface

### Publish layers

Each one of the different implementations of the different types of layers must be published in order to be used in each of the instances of the framework.
There are three ways to publish the implementation of a layer:

 - Explicitly, somewhere in the code of the application must explicitly indicate the publication of the layer
 ``` java
 Layers.publishLayer(LayerTestA.class);
 ```
 - Deploying a jar file as plugin, this action publish all the layers into the jar automatically.
 - All the distributed layers published by other node into the cluster are public for all the nodes.

### Invoke layers

In order to invoke a layer you have to indicate the class object that refers to the interface that you want to find and the name of the implementation that you want to execute

### Example

We're going to create the set of classes to build an example of publishing and using a layer with a couple of implementations
The case study of the example, is the calculation of the average of a set of decimal numbers, one of the implementations is the arithmetic average and the other implementation is the harmonic average.

#### Layer interface definition
``` java
import org.hcjf.layers.LayerInterface;

public interface AverageCalculation extends LayerInterface {

    Double calculate(Double... samples);

}
```

#### Arithmetic Average
``` java
import org.hcjf.layers.Layer;

public class ArithmeticAverage extends Layer implements AverageCalculation {

    public ArithmeticAverage() {
        super("arithmetic");
    }

    public Double calculate(Double... samples) {
        return DoubleStream.of(samples).sum() / samples.length;
    }
}
```

#### Harmonic Average
``` java
import org.hcjf.layers.Layer;

public class HarmonicAverage extends Layer implements AverageCalculation {

    public HarmonicAverage() {
        super("harmonic");
    }

    public Double calculate(Double... samples) {
        sum result = 0;
        for(int i = 0: i < samples.length; i++) {
            sum += 1/samples[i];
        }
        return samples.length / sum;
    }
}
```

#### Use case for the average calculation
``` java
public static void main(String[] args) {

    Double[] data = {23.0, 34.2, 0.45, 14.6, 16.4, 9.0, -45.3};

    //... In this block the layers are published
    Layers.publish(HarmonicAverage.class);
    Layers.publish(ArithmeticAverage.class);
    //...


    //... In this block the layers are consumed
    AverageCalculation averageCalculation = Layers.get(AverageCalculation.class, args[0]);
    System.out.println("Result: " + everageCalculation.calculate(data));
}
```
As you can see in the example, depending on the name that is indicated in the method 'get ()' of the class Layers will be the implementation that will return the framework, but as all the implementations respect the same interface, it is possible to keep the same code modifying implementations

## Introspection cache
This arises because all the generic processes that you want to develop involve introspection, that's why the framework contains a utility that allows you to store the result of an indexed introspection process with a name, which I call this cache of introspection.
The main virtue of this mechanism is to improve the speed of access to the introspection by decreasing ten times the speed, for this reason this mechanism is expelled by all the modules of the framework.
As with any cache, the first call to introspection with a particular filter takes the same amount of time as doing the process without a cache, but the time in the next calls decreases noticeably

### Example
``` java
package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

    private static class TestEntity extends InheritanceTestEntity<String, Integer> {

        private Integer integer;
        private Map<String, String> map1;
        private Map<String, Set<String>> map2;
        private Collection<String> collection1;

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
```

## Queries [queries]
Another thing very interesting into the HCJF is the query api. This api provides a very clear interface based on ANSI SQL which allows write our query in the same way that we would using a SQL data base, but this query run over our micro-services solucion using differents data soucers.
This api is composed by four components:

 - [Compiler](#query-compiler)
 - [Data sources](#query-data-sources)
 - [Functions](#query-functions)
 - [Resolution engine](#query-resolution-engine)

### Compiler [query-compiler]
This component gives the capability of parse any query int the ANSI SQL format and creates a Query object as result. The Query object contains all the differents parts of the query, each part organized into the object and each of this can be iterated and obtained in order to inspect the query and take decisions base on this information.
Then a simple example of query parser
``` java
Query query = Query.compile("SELECT * FROM resource");
```

### Data sources [query-data-sources]

### Functions [query-functions]

### Resolution engine [query-resolution-engine]

## Bson Parcelable
Any object that implements the interface 'org.hcjf.utils.bson.BsonParcelable' has the ability to serialize its internal model in [bson](http://bsonspec.org/) format, for this by introspection in each of the methods of type 'get' of the class of the object, the values corresponding to the object are obtained.
Just as you can serialize the object, you can also obtain the instance from the block of bytes generated from the serialization of the original object, for this purpose you will do introspection in the object class that you want to reconstruct, on each of the methods of the type 'set'.
For all this we must bear in mind that an object that is intended to give the ability to be serializable in bson format must specify each of the methods 'set' and 'get' of the internal elements that are required for serialization

### Example
Creating some bson-parcelable classes

``` java
public static class Test2 implements BsonParcelable {

        private List<Path> paths;

        public List<Path> getPaths() {
            return paths;
        }

        public void setPaths(List<Path> paths) {
            this.paths = paths;
        }
}
```

``` java
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
```

``` java
public class TestClass implements BsonParcelable {

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
```

We gonna create a little serializable class in order to test the java serialized classes into the bson format
``` java
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
```
In the next block we show some of the most common uses for the bson-serializable objects.
``` java
package org.hcjf.utils;

import org.hcjf.bson.BsonDocument;
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

        Test2 t2 = new Test2();
        t2.setPaths(new ArrayList<>());
        t2.getPaths().add(new Path(null, null, new ArrayList<>()));
        t2.toBson();

        t2 = new Test2();
        t2.setPaths(null);
        t2.toBson();


    }

}
```

All the previous classes are in the set of test cases. [BsonParcelableTest](https://github.com/javaito/HolandaCatalinaFw/blob/master/src/test/java/org/hcjf/utils/BsonParcelableTest.java)

