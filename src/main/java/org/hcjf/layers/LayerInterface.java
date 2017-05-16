package org.hcjf.layers;

import java.lang.reflect.InvocationHandler;

/**
 * @author javaito
 *
 */
public interface LayerInterface extends InvocationHandler {

    /**
     * Return the layer implementation name.
     * @return Layer implementation name.
     */
    public String getImplName();

    /**
     * Return if the layer is stateful or not.
     * @return Stateful
     */
    public boolean isStateful();

}
