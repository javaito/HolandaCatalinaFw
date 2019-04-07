package org.hcjf.layers.query.functions;

import java.util.Collection;
import java.util.Map;

public class MaxAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer implements NumberSetFunction {

    public static final String NAME = "aggregateMax";

    public MaxAggregateFunctionLayer() {
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
                    accumulatedValue = Double.MIN_VALUE;
                    accumulatedValue = accumulateFunction(accumulatedValue, new Object[]{((Map)row).get(fieldName)}, (A,V)-> A.compareTo(V) >= 0 ? A : V)[1];
                    ((Map)row).put(alias, accumulatedValue);
                }
            } catch (Exception ex){
                throw new RuntimeException("Sum aggregate function fail", ex);
            }
        } else {
            throw new IllegalArgumentException("Sum aggregate function need at leas one parameter");
        }
        return result;
    }
}
