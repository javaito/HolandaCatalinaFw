package org.hcjf.layers.query.functions;

import org.hcjf.layers.LayerInterface;

/**
 * These kind of layers are using for the query implementation to resolve
 * the functions specified into the each query.
 * @author javaito.
 */
public interface QueryFunctionLayerInterface extends LayerInterface {

    /**
     * Evaluates the specific function.
     * @param functionName Function name.
     * @param parameters Function's parameters.
     * @return Function result.
     */
    public Object evaluate(String functionName, Object... parameters);

}
