package org.hcjf.layers.query.functions;

import org.hcjf.layers.LayerInterface;

import java.util.Collection;

/**
 * @author javaito
 */
public interface QueryAggregateFunctionLayerInterface extends LayerInterface {

    /**
     * Evaluates the specific function.
     * @param resultSet Result set obtained for the query evaluation.
     * @param parameters Function's parameters.
     * @param alias Alias of the function
     * @return Function result.
     */
    Collection evaluate(String alias, Collection resultSet, Object... parameters);

}
