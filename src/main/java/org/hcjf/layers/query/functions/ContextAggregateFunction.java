package org.hcjf.layers.query.functions;

import java.util.Collection;
import java.util.Map;

public class ContextAggregateFunction extends BaseQueryAggregateFunctionLayer {

    private static final String NAME = "aggregateContext";

    public ContextAggregateFunction() {
        super(NAME);
    }

    /**
     * Evaluates the specific function.
     * @param alias      Alias of the function
     * @param resultSet  Result set obtained for the query evaluation.
     * @param parameters Function's parameters.
     * @return Function result.
     */
    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Object value;
        for (Object row : resultSet) {
            value = resolveValue(row, parameters[0]);
            ((Map)row).put(alias, value);
        }
        return resultSet;
    }
}
