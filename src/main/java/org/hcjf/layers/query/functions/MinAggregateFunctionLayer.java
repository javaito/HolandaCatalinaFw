package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.query.Query;

import java.util.Collection;
import java.util.Map;

public class MinAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer implements NumberSetFunction {

    public static final String NAME = "aggregateMin";

    public MinAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = resultSet;
        if(parameters.length >= 1) {
            try {
                String path = getPath(parameters[0]);
                Number accumulatedValue;
                for(Object row : resultSet) {
                    accumulatedValue = Double.MAX_VALUE;
                    accumulatedValue = accumulateFunction(accumulatedValue, new Object[]{((Map)row).get(path)}, (A,V)-> A.compareTo(V) <= 0 ? A : V)[1];
                    ((Map)row).put(alias, accumulatedValue);
                }
            } catch (Exception ex){
                throw new HCJFRuntimeException("Min aggregate function fail", ex);
            }
        } else {
            throw new HCJFRuntimeException("Min aggregate function need at leas one parameter");
        }
        return result;
    }
}
