package org.hcjf.layers.query.functions;

import org.hcjf.layers.query.Enlarged;
import org.hcjf.layers.query.Query;

import java.util.Collection;

public class EvalExpressionAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer implements NumberSetFunction {

    private static final String NAME = "aggregateEvalExpression";

    public EvalExpressionAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Object[] resolvedParameters = new Object[parameters.length];
        for(Object row : resultSet) {
            for (int i = 0; i < parameters.length; i++) {
                if(parameters[i] instanceof Query.QueryReturnField) {
                    resolvedParameters[i] = ((Query.QueryReturnField)parameters[i]).resolve(row);
                } else {
                    resolvedParameters[i] = parameters[i];
                }
            }
            ((Enlarged)row).put(alias, evalExpression(resolvedParameters));
        }
        return resultSet;
    }
}
