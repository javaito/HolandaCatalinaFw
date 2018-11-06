package org.hcjf.layers;

import java.lang.reflect.Method;

/**
 * This interface indicates that the implementation is able to adapt the arguments to force the call.
 */
public interface AdaptableLayer {

    /**
     * Adapt the arrays of arguments to force the layer call.
     * @param method Method to call.
     * @param args Original array of arguments.
     * @return New array of arguments.
     */
    default Object[] adaptArguments(Method method, Object[] args) {
        return args;
    }

}
