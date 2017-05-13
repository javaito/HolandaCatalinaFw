package org.hcjf.layers.query.functions;

import org.hcjf.layers.LayerInterface;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public interface QueryFunctionInterface extends LayerInterface {

    public Object evaluate(String functionName, Object... parameters);

}
