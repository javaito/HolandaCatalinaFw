package org.hcjf.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

/**
 * This abstract class contains all the implementations and
 * the interfaces that describe the behavior of the system service.
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class Service<C extends ServiceConsumer> {

    private final String serviceName;
    private final ThreadFactory serviceThreadFactory;

    /**
     * Service constructor.
     * @param serviceName Name of the service, can't be null.
     * @throws NullPointerException If the name is null.
     */
    protected Service(String serviceName) {
        if(serviceName == null) {
            throw new NullPointerException("Service name can't be null");
        }

        this.serviceName = serviceName;
        this.serviceThreadFactory = new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return null;
            }

        };
        init();
        SystemServices.instance.register(this);
    }

    /**
     * Return the internal thread factory for the services.
     * @return Thread factory.
     */
    protected final ThreadFactory getServiceThreadFactory() {
        return serviceThreadFactory;
    }

    /**
     * Return the service name.
     * @return Service name.
     */
    public final String getServiceName() {
        return serviceName;
    }

    /**
     * This method will be called immediately after
     * of the execution of the service's constructor method
     */
    protected void init(){};

    /**
     * This method will be called immediately after the static
     * method 'shutdown' of the class has been called.
     */
    protected void shutdown(){};

    /**
     * This method register the consumer in the service.
     */
    public abstract void registerConsumer(C consumer);

    /**
     * This internal class contains all the services registered
     * in the system.
     */
    private static class SystemServices {

        private static final SystemServices instance;

        static {
            instance = new SystemServices();
        }

        private final Map<String, Service> services;

        /**
         * Constructor.
         */
        private SystemServices() {
            services = new HashMap<>();
        }

        /**
         * This method save the instance of the service in the internal
         * map indexed by the name of the service.
         * @param service Instance of the service.
         */
        private void register(Service service) {
            services.put(service.getServiceName(), service);
        }
    }
}
