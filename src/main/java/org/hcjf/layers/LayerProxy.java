package org.hcjf.layers;

import java.lang.reflect.Method;

/**
 * This class wrap the layer invocation with two method, one
 * before the invocation and other after invocation.
 * @author javaito
 *
 */
public interface LayerProxy {

    /**
     * This method is called before of layer invocation.
     * @param method Layer's method to invoke.
     * @param params Parameter's method.
     * @return Proxy interceptor instance.
     */
    ProxyInterceptor onBeforeInvoke(Method method, Object... params);

    /**
     * This method is called after of layer invocation.
     * @param method Layer's method to invoke.
     * @param result Layer's invocation result.
     * @param params Parameter's method.
     */
    void onAfterInvoke(Method method, Object result, Object... params);

    /**
     * This class is the result of the before invoke method.
     */
    class ProxyInterceptor {

        private final boolean cached;
        private final Object result;

        public ProxyInterceptor(boolean cached, Object result) {
            this.cached = cached;
            this.result = result;
        }

        public ProxyInterceptor() {
            this(false, null);
        }

        /**
         * Return true if the proxy intercepts the invocation.
         * @return Intercept value.
         */
        public boolean isCached() {
            return cached;
        }

        /**
         * Return the intercepted value.
         * @return Intercepted value.
         */
        public Object getResult() {
            return result;
        }
    }

}
