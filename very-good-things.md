# Very good things!

In this section I want to show some of the most interesting things that are part of the internal structure of the framework, some of these things provide performance and scalability to the construction and maintenance of the project

## Internal services

An internal service is an internal component of the framework responsible for performing a specific task that maintains communication with the application layer through observers that provide an interface with the service. This type of services start to work on demand, if they are not used they do not add load to the framework instance
Para crear un nuevo servicio interno solo hay que escribir dos clases una debe implementar la interfaz [ServiceConsumer](/src/main/java/org/hcjf/service/ServiceConsumer.java) y la otra clase que hay que escibir tiene que extender la clase [Service](/src/main/java/org/hcjf/service/Service.java)

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
 - Deployin a jar file as plugin, this action publish all the layers into the jar automatically.
 - All the distributed layers published by other node into the cluster are public for all the nodes.

### Invoke layers

In order to invoke a layer you have to indicate the class object that refers to the interface that you want to find and the name of the implementation that you want to execute

### Example

We're going to create the set of classes to build an example of publishing and using a layer with a couple of implementations
The case study of the example, is the calculation of the average of a set of decimal numbers, one of the implementations is the arithmetic average and the other implementation is the harmonic average.

### Layer interface definition
``` java
import org.hcjf.layers.LayerInterface;

public interface AverageCalculation extends LayerInterface {

    Double calculate(Double... samples);

}
```

### Arithmetic Average
``` java
import org.hcjf.layers.Layer;

public class ArithmeticAverage extends Layer implements AverageCalculation {

    public ArithmeticAverage() {
        super("arithmetic");
    }

    public Double calculation(Double... samples) {
        return DoubleStream.of(samples).sum() / samples.length;
    }
}
```

### Harmonic Average
``` java
import org.hcjf.layers.Layer;

public class HarmonicAverage extends Layer implements AverageCalculation {

    public HarmonicAverage() {
        suprt("harmonic");
    }

    public Double calculation(Double... samples) {
        sum result = 0;
        for(int i = 0: i < samples.length; i++) {
            sum += 1/samples[i];
        }
        return samples.length / sum;
    }
}
```

### Use case for the average calculation


## Introspection cache

## Bson Parcelable

## Network