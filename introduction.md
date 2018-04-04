# Introduction

## What are micro-services?
Micro-services - also known as the micro-service architecture - is an architectural style that structures an application as a collection of loosely coupled services, which implement business capabilities. The micro-service architecture enables the continuous delivery/deployment of large, complex applications. It also enables an organization to evolve its technology stack.

## Micro-services are not a silver bullet
The micro-service architecture is not a silver bullet. It has several drawbacks. Moreover, when using this architecture there are numerous issues that you must address. The microservice architecture pattern language is a collection of patterns for applying the micro-service architecture. It has two goals:

 - The pattern language enables you to decide whether micro-services are a good fit for your application.
 - The pattern language enables you to use the micro-service architecture successfully.

## Where to start?
A good starting point is the [Monolithic Architecture pattern](http://microservices.io/patterns/monolithic.html) which is the traditional architectural style that is still a good choice for many applications. It does, however, have numerous limitations and issues and so a better choice for large/complex applications is the [Micro-service architecture pattern](http://microservices.io/patterns/microservices.html).

## See more...
[Micro-service Architecture](http://microservices.io/index.html)

## HCJFs Approach
This implementation allows developers to build their solutions thinking about the specific problem without worrying about all the details inherent to the architecture of micro-services and achieve that old goal-oriented design desire where our components have a high cohesion and low coupling.
To achieve this we rely on a set of tools provided by the framework that enhance our solutions to be more than a porsion of code running on our machine and become part of a distributed and self-contained micro-service

### In my opinion, micro-services
I believe that micro-services are the conclusion of the effort of many people trying to make their platforms maintainable, self-sufficient, scalable and able to handle a demand for information tending to infinity. All this is being achieved but personally I do not like that the solutions to particular problems get dirty with details related to the technology used or problems of the environment where they are working.