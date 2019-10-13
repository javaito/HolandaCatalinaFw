package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.query.JoinableMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SumAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer implements NumberSetFunction {

    public static final String NAME = "aggregateSum";

    public SumAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = resultSet;
        if(parameters.length >= 1) {
            try {
                boolean accumulate = parameters.length >= 2 && (boolean) parameters[1];
                boolean group = parameters.length >= 3 && (boolean) parameters[2];
                Number value;
                Number accumulatedValue = 0;
                for (Object row : resultSet) {
                    value = accumulateFunction(accumulatedValue,
                            new Object[]{resolveValue(row, parameters[0])}, (A, V) -> A.add(V))[1];
                    if(!group) {
                        if(accumulate) {
                            accumulatedValue = accumulatedValue.doubleValue() + value.doubleValue();
                            ((Map) row).put(alias, accumulatedValue);
                        } else {
                            ((Map) row).put(alias, value);
                        }
                    }
                }

                if(group) {
                    Collection<JoinableMap> newResultSet = new ArrayList<>();
                    JoinableMap count = new JoinableMap(new HashMap<>(), alias);
                    count.put(alias, accumulatedValue);
                    newResultSet.add(count);
                    result = newResultSet;
                }
            } catch (Exception ex){
                throw new HCJFRuntimeException("Sum aggregate function fail", ex);
            }
        } else {
            throw new HCJFRuntimeException("Sum aggregate function need at leas one parameter");
        }
        return result;
    }
}
