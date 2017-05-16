package org.hcjf.layers.query.functions;

import org.hcjf.layers.LayerInterface;

/**
 * @author javaito.
 *
 */
public interface QueryFunctionInterface extends LayerInterface {

    public Object evaluate(String functionName, Object... parameters);

}
