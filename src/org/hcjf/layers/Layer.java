package org.hcjf.layers;

import org.hcjf.service.ServiceThread;

import java.lang.reflect.Method;

/**
 * All the layer implementation extends this class, and this class is a proxy
 * between the layer client and implementation.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Layer implements LayerInterface {

    private final String implName;
    private final boolean stateful;

    public Layer(String implName, boolean stateful) {
        if(implName == null) {
            throw new IllegalArgumentException("Implementation name can't be null");
        }
        this.implName = implName;
        this.stateful = stateful;
    }

    public Layer(String implName) {
        this(implName, true);
    }

    /**
     * Return the layer implementation name.
     * @return Layer implementation name.
     */
    @Override
    public final String getImplName() {
        return implName;
    }

    /**
     * Return if the layer is stateful or not.
     * @return Stateful
     */
    @Override
    public final boolean isStateful() {
        return stateful;
    }

    /**
     * This method must be override to add restrictions over particular
     * implementations of the layers.
     */
    public Access checkAccess(){
        return new Access(true);
    }

    /**
     * This method intercepts the call to layer implementation and
     * save some information about the thread behavior.
     * @param proxy Object to be called.
     * @param method Method to be called.
     * @param args Method to invoke the method.
     * @return Return the value returned for the proxy method.
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Access access = checkAccess();

        if(access == null) {
            throw new SecurityException("Access null");
        }
        if(!access.granted) {
            if(access.message != null && access.getThrowable() != null) {
                throw new SecurityException(access.getMessage(), access.getThrowable());
            } else if(access.getMessage() != null) {
                throw new SecurityException(access.getMessage());
            } else if(access.getThrowable() != null) {
                throw new SecurityException(access.getThrowable());
            }
        }

        ServiceThread serviceThread = null;
        if(Thread.currentThread() instanceof ServiceThread) {
            serviceThread = (ServiceThread) Thread.currentThread();
        }

        Object result;
        if(serviceThread != null) {
            serviceThread.putLayer(getClass());
        }
        result = method.invoke(this, args);
        if(serviceThread != null) {
            serviceThread.removeLayer();
        }
        return result;
    }

    /**
     * This class represents the access resume of the layer.
     */
    public final static class Access {

        private final boolean granted;
        private final String message;
        private final Throwable throwable;

        public Access(boolean granted, String message, Throwable throwable) {
            this.granted = granted;
            this.message = message;
            this.throwable = throwable;
        }

        public Access(boolean granted, String message) {
            this(granted, message, null);
        }

        public Access(boolean granted) {
            this(granted, null, null);
        }

        /**
         * It returns true if access has been granted and false otherwise.
         * @return Granted resume.
         */
        public boolean isGranted() {
            return granted;
        }

        /**
         * Return a message associated to the granted resume.
         * @return Message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Return the throwable associated to the granted resume.
         * @return Throwable.
         */
        public Throwable getThrowable() {
            return throwable;
        }
    }
}
