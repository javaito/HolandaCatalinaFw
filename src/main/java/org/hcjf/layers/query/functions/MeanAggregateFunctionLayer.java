package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;

public class MeanAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer implements NumberSetFunction {

    public static final String NAME = "aggregateMean";
    public static final String ARITHMETIC = "arithmetic";
    public static final String GEOMETRIC = "geometric";
    public static final String HARMONIC = "harmonic";

    public MeanAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = resultSet;
        if(parameters.length >= 1) {
            try {
                String fieldName = (String) parameters[0];
                Number accumulatedValue = 0;
                Number[] functionResult;
                String meanKind = parameters.length == 1 ? ARITHMETIC : (String) parameters[1];
                for(Object row : resultSet) {
                    switch (meanKind) {
                        case GEOMETRIC: {
                            functionResult = accumulateFunction(accumulatedValue, new Object[]{((Map)row).get(fieldName)}, (A, V)->A.multiply(V));
                            functionResult[1] = Math.pow(functionResult[1].doubleValue(),  1 / functionResult[0].doubleValue());
                            break;
                        }
                        case HARMONIC: {
                            functionResult = accumulateFunction(accumulatedValue, new Object[]{((Map)row).get(fieldName)}, (A, V)->A.add(new BigDecimal(1).
                                    divide(V, SystemProperties.getInteger(SystemProperties.Query.Function.BIG_DECIMAL_DIVIDE_SCALE), RoundingMode.HALF_EVEN)));
                            functionResult[1] = functionResult[0].doubleValue() / functionResult[1].doubleValue();
                            break;
                        }
                        default: {
                            functionResult = accumulateFunction(accumulatedValue, new Object[]{((Map)row).get(fieldName)}, (A, V)->A.add(V));
                            functionResult[1] = functionResult[1].doubleValue() / functionResult[0].doubleValue();
                        }
                    }
                    ((Map)row).put(alias, functionResult[1]);
                }
            } catch (Exception ex){
                throw new HCJFRuntimeException("Mean aggregate function fail", ex);
            }
        } else {
            throw new HCJFRuntimeException("Mean aggregate function need at leas two parameter");
        }
        return result;
    }
}
