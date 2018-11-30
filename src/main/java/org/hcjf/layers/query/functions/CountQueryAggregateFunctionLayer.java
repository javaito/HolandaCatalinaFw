package org.hcjf.layers.query.functions;

import java.util.Collection;

/**
 * @author javaito
 */
public class CountQueryAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer {

    private static final String NAME = "count";

    public CountQueryAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Object evaluate(Collection resultSet, Object... parameters) {
        return resultSet.size();
    }
}
