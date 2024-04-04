package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.properties.SystemProperties;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

public class MaxAggregateFunctionLayer extends BaseQueryAggregateFunctionLayer implements NumberSetFunction {

    public static final String NAME = "aggregateMax";

    public MaxAggregateFunctionLayer() {
        super(NAME);
    }

    @Override
    public Number applyFunction(Number firstNumber, Number secondNumber, BiFunction<BigDecimal, BigDecimal, Number> function) {
        Number result;
        if (firstNumber.equals(Double.MIN_VALUE) && this.getClass().equals(MaxAggregateFunctionLayer.class)){
            result = new BigDecimal(secondNumber.doubleValue());
        } else {
            result = function.apply(new BigDecimal(firstNumber.doubleValue()),
                    new BigDecimal(secondNumber.doubleValue()));
        }

        Boolean round = SystemProperties.getBoolean(SystemProperties.Query.Function.MATH_OPERATION_RESULT_ROUND);
        if (result instanceof BigDecimal && round) {
            Integer mathContext = SystemProperties.getInteger(SystemProperties.Query.Function.MATH_OPERATION_RESULT_ROUND_CONTEXT);
            switch (mathContext) {
                case 32: result = ((BigDecimal)result).round(MathContext.DECIMAL32); break;
                case 64: result = ((BigDecimal)result).round(MathContext.DECIMAL64); break;
                case 128: result = ((BigDecimal)result).round(MathContext.DECIMAL128); break;
            }
        }

        return result;
    }

    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = resultSet;
        if(parameters.length >= 1) {
            try {
                Number accumulatedValue;
                for(Object row : resultSet) {
                    accumulatedValue = Double.MIN_VALUE;
                    accumulatedValue = accumulateFunction(accumulatedValue, new Object[]{resolveValue(row, parameters[0])}, (A,V)-> A.compareTo(V) >= 0 ? A : V)[1];
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
