package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;

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
                String fieldName = getParameter(0, parameters);
                Number accumulatedValue;
                for(Object row : resultSet) {
                    accumulatedValue = Double.MIN_VALUE;
                    accumulatedValue = accumulateFunction(accumulatedValue, new Object[]{((Map)row).get(fieldName)}, (A,V)-> A.compareTo(V) >= 0 ? A : V)[1];
                    ((Map)row).put(alias, accumulatedValue);
                }
            } catch (Exception ex){
                throw new HCJFRuntimeException("Max aggregate function fail", ex);
            }
        } else {
            throw new HCJFRuntimeException("Max aggregate function need at leas one parameter");
        }
        return result;
    }
}
