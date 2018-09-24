package org.hcjf.layers;

import org.hcjf.layers.distributed.DistributedLayer;

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
    String getImplName();

    /**
     * Returns true if the layer is stateful or false in the otherwise.
     * @return Stateful
     */
    boolean isStateful();

    /**
     * Returns true if the layer is a plugin or false in the otherwise.
     * @return Plugin status.
     */
    boolean isPlugin();

    /**
     * Returns true if the layer implementation is instance of {@link DistributedLayer}.
     * @return True if the layer implementation is instance of {@link DistributedLayer} and false in the otherwise.
     */
    default boolean isDistributed() {
        return this instanceof DistributedLayer;
    }

}
