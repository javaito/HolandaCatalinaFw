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
                if(parameters.length == 1) {
                    Number sumValue;
                    for (Object row : resultSet) {
                        sumValue = accumulateFunction(0,
                                new Object[]{resolveValue(row, parameters[0])}, (A, V) -> A.add(V))[1];
                        ((Map) row).put(alias, sumValue);
                    }
                } else {
                    boolean group = true;
                    if (parameters.length >= 2) {
                        group = getParameter(1, parameters);
                    }
                    boolean accumulate = false;
                    if (parameters.length == 3) {
                        accumulate = getParameter(2, parameters);
                    }
                    Number accumulatedValue = 0;
                    for (Object row : resultSet) {
                        accumulatedValue = accumulateFunction(accumulatedValue,
                                new Object[]{resolveValue(row, parameters[0])}, (A, V) -> A.add(V))[1];
                        if (!group) {
                            if (accumulate) {
                                ((Map) row).put(alias, accumulatedValue);
                            }
                        }
                    }

                    if (group) {
                        Collection<JoinableMap> newResultSet = new ArrayList<>();
                        JoinableMap count = new JoinableMap(new HashMap<>(), alias);
                        count.put(alias, accumulatedValue);
                        newResultSet.add(count);
                        result = newResultSet;
                    } else {
                        if (!accumulate) {
                            for (Object row : resultSet) {
                                ((Map) row).put(alias, accumulatedValue);
                            }
                        }
                    }
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
