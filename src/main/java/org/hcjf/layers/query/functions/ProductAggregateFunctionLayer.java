package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;

import java.util.Collection;
import java.util.Map;

public class ProductAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer implements NumberSetFunction {

    private static final String NAME = "aggregateProduct";

    public ProductAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = resultSet;
        if(parameters.length >= 1) {
            try {
                Number accumulatedValue;
                for(Object row : resultSet) {
                    accumulatedValue = 1;
                    accumulatedValue = accumulateFunction(accumulatedValue, new Object[]{resolveValue(row, parameters[0])}, (A, V) -> A.multiply(V))[1];
                    ((Map) row).put(alias, accumulatedValue);
                }
            } catch (Exception ex){
                throw new HCJFRuntimeException("Product aggregate function fail", ex);
            }
        } else {
            throw new HCJFRuntimeException("Product aggregate function need at leas one parameter");
        }
        return result;
    }
}
