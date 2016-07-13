package org.hcjf.layers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class Layer implements InvocationHandler {

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
     *
     * @return
     */
    public final String getImplName() {
        return implName;
    }

    /**
     *
     * @return
     */
    public final boolean isStateful() {
        return stateful;
    }

    /**
     *
     */
    public void checkAccess(){
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
        //TODO: Store the information over service thread.
        return method.invoke(this, args);
    }
}
