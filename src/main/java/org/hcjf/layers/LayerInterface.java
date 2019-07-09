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
     * Returns a regex to math, this regex only gonna by used if the get layer method not found any layer with the
     * specific name.
     * @return Regex value. Null by default.
     */
    default String getRegex() {
        return null;
    }

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
