package org.hcjf.layers.query.functions;

import java.util.Set;

/**
 * @author javaito
 */
public class CountQueryAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer {

    private static final String NAME = "count";

    public CountQueryAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Object evaluate(Set resultSet, Object... parameters) {
        return resultSet.size();
    }
}
