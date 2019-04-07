package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.MathIntrospection;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * This layer implements all the math functions to invoke from the query scope.
 * @author javaito
 */
public class MathQueryFunctionLayer extends BaseQueryFunctionLayer implements NumberSetFunction {

    private static final String ARITHMETIC = "arithmetic";
    private static final String GEOMETRIC = "geometric";
    private static final String HARMONIC = "harmonic";
    private static final String MEDIAN = "median";

    private static final String SUM = "sum";
    private static final String PRODUCT = "product";
    private static final String BYTE_VALUE = "byteValue";
    private static final String SHORT_VALUE = "shortValue";
    private static final String INTEGER_VALUE = "integerValue";
    private static final String LONG_VALUE = "longValue";
    private static final String FLOAT_VALUE = "floatValue";
    private static final String DOUBLE_VALUE = "doubleValue";
    private static final String MEAN = "mean";
    private static final String EVAL_EXPRESSION = SystemProperties.get(SystemProperties.Query.Function.MATH_EVAL_EXPRESSION_NAME);

    public MathQueryFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.MATH_FUNCTION_NAME));

        for(String functionName : MathIntrospection.getMethodsSet()){
            addFunctionName(functionName);
        }

        addFunctionName(SUM);
        addFunctionName(PRODUCT);
        addFunctionName(BYTE_VALUE);
        addFunctionName(SHORT_VALUE);
        addFunctionName(INTEGER_VALUE);
        addFunctionName(LONG_VALUE);
        addFunctionName(FLOAT_VALUE);
        addFunctionName(DOUBLE_VALUE);
        addFunctionName(MEAN);
        addFunctionName(EVAL_EXPRESSION);
    }

    /**
     * Evaluates the math function.
     * @param functionName Function name.
     * @param parameters Function's parameters.
     * @return Returns the numeric value that represents the result of the function evaluation.
     */
    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result;
        switch (functionName) {
            case SUM: {
                Number[] accumulator = accumulateFunction(0, parameters, BigDecimal::add);
                result = accumulator[1];
                break;
            }
            case PRODUCT: {
                Number[] accumulator = accumulateFunction(1, parameters, BigDecimal::add);
                result = accumulator[1];
                break;
            }
            case MEAN: {
                Number accumulatedValue = 0;
                Number[] functionResult;
                String function = parameters.length == 1 ? ARITHMETIC : (String) parameters[1];
                if(function.equals(GEOMETRIC)) {
                    functionResult = accumulateFunction(accumulatedValue, new Object[]{parameters[0]}, (A, V)->A.multiply(V));
                    result = Math.pow(functionResult[1].doubleValue(),  1 / functionResult[0].doubleValue());
                } else if(function.equals(HARMONIC)) {
                    functionResult = accumulateFunction(accumulatedValue, new Object[]{parameters[0]}, (A, V)->A.add(V.pow(-1)));
                    result = functionResult[0].doubleValue() / functionResult[1].doubleValue();
                } else if(function.equals(MEDIAN)) {
                    if(parameters[0] instanceof Collection || parameters[0].getClass().isArray()) {
                        Collection collection = parameters[0] instanceof Collection ? (Collection) parameters[0] : Arrays.asList(parameters[0]);
                        if(collection.isEmpty()) {
                            result = 0;
                        } else if(collection.size() == 1) {
                            result = collection.stream().iterator().next();
                        } else {
                            int size = collection.size();
                            result = collection.stream().sorted().skip(size / 2).findFirst().get();
                        }
                    } else {
                        result = parameters[0];
                    }
                } else {
                    functionResult = accumulateFunction(accumulatedValue, new Object[]{parameters[0]}, (A, V)->A.add(V));
                    result = functionResult[1].doubleValue() / functionResult[0].doubleValue();
                }
                break;
            }
            case BYTE_VALUE: result = ((Number)checkSize(1, parameters)[0]).byteValue(); break;
            case SHORT_VALUE: result = ((Number)checkSize(1, parameters)[0]).shortValue(); break;
            case INTEGER_VALUE: result = ((Number)checkSize(1, parameters)[0]).intValue(); break;
            case LONG_VALUE: result = ((Number)checkSize(1, parameters)[0]).longValue(); break;
            case FLOAT_VALUE: result = ((Number)checkSize(1, parameters)[0]).floatValue(); break;
            case DOUBLE_VALUE: result = ((Number)checkSize(1, parameters)[0]).doubleValue(); break;
            default: {
                if(functionName.equals(EVAL_EXPRESSION)) {
                    result = evalExpression(parameters);
                } else {
                    result = MathIntrospection.invoke(functionName, parameters);
                }
            }
        }
        return result;
    }

}
