package org.hcjf.layers.query.functions;

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
                String fieldName = (String) parameters[0];
                Number accumulatedValue;
                for(Object row : resultSet) {
                    accumulatedValue = 1;
                    accumulatedValue = accumulateFunction(accumulatedValue, new Object[]{((Map)row).get(fieldName)}, (A, V)->A.multiply(V))[1];
                    ((Map)row).put(alias, accumulatedValue);
                }
            } catch (Exception ex){
                throw new RuntimeException("Product aggregate function fail", ex);
            }
        } else {
            throw new IllegalArgumentException("Product aggregate function need at leas one parameter");
        }
        return result;
    }
}
