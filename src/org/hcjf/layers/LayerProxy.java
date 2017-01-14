package org.hcjf.layers;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class wrap the layer invocation with two method, one
 * before the invocation and other after invocation.
 * @author javaito
 * @email javaito@gmail.com
 */
public interface LayerProxy {

    /**
     * This method is called before of layer invocation.
     * @param method Layer's method to invoke.
     * @param params Parameter's method.
     */
    public void onBeforeInvoke(Method method, Object... params);

    /**
     * This method is called after of layer invocation.
     * @param method Layer's method to invoke.
     * @param result Layer's invocation result.
     * @param params Parameter's method.
     */
    public void onAfterInvoke(Method method, Object result, Object... params);

}
