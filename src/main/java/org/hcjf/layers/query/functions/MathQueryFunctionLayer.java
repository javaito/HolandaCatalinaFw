package org.hcjf.layers.query.functions;

import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.MathIntrospection;

import java.util.HashSet;
import java.util.Set;

/**
 * This layer implements all the math functions to invoke from the query scope.
 * @author javaito
 */
public class MathQueryFunctionLayer extends Layer implements QueryFunctionLayerInterface {

    private static final String SUM = "sum";
    private static final String PRODUCT = "product";
    private static final String BYTE_VALUE = "byteValue";
    private static final String SHORT_VALUE = "shortValue";
    private static final String INTEGER_VALUE = "integerValue";
    private static final String LONG_VALUE = "longValue";
    private static final String FLOAT_VALUE = "floatValue";
    private static final String DOUBLE_VALUE = "doubleValue";
    private static final String EVAL_EXPRESSION = SystemProperties.get(SystemProperties.Query.Function.MATH_EVAL_EXPRESSION_NAME);

    private final Set<String> aliases;

    public MathQueryFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) +
                SystemProperties.get(SystemProperties.Query.Function.MATH_LAYER_NAME));
        aliases = new HashSet<>();
        for(String functionName : MathIntrospection.getMethodsSet()){
            aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + functionName);
        }

        //Add static aliases
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + SUM);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PRODUCT);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + BYTE_VALUE);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + SHORT_VALUE);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + INTEGER_VALUE);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + LONG_VALUE);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + FLOAT_VALUE);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + DOUBLE_VALUE);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + EVAL_EXPRESSION);
    }

    /**
     * Returns all the aliases for this layer.
     * @return Layers aliases.
     */
    @Override
    public Set<String> getAliases() {
        return aliases;
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
                Number accumulator = 0;
                for(Object numericParameter : parameters) {
                    accumulator = accumulator.doubleValue() + ((Number) numericParameter).doubleValue();
                }
                result = accumulator;
                break;
            }
            case PRODUCT: {
                Number accumulator = 1;
                for(Object numericParameter : parameters) {
                    accumulator = accumulator.doubleValue() * ((Number) numericParameter).doubleValue();
                }
                result = accumulator;
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

    /**
     * This method iterate all the parameter to evaluate math expression. Each parameter contains
     * a number value or operator.
     * @param parameters Math expression parameters.
     * @return Returns the number that results of evaluate the math expression.
     */
    private Number evalExpression(Object... parameters) {
        double result = 0.0;
        String currentOperation = SystemProperties.get(SystemProperties.Query.Function.MATH_ADDITION);
        for(Object parameter : parameters) {
            if(parameter instanceof String) {
                currentOperation = (String) parameter;
            } else if(parameter instanceof Number) {
                if(currentOperation.equals(SystemProperties.get(SystemProperties.Query.Function.MATH_ADDITION))) {
                    result += ((Number)parameter).doubleValue();
                } else if(currentOperation.equals(SystemProperties.get(SystemProperties.Query.Function.MATH_SUBTRACTION))) {
                    result -= ((Number)parameter).doubleValue();
                } else if(currentOperation.equals(SystemProperties.get(SystemProperties.Query.Function.MATH_MULTIPLICATION))) {
                    result *= ((Number)parameter).doubleValue();
                } else if(currentOperation.equals(SystemProperties.get(SystemProperties.Query.Function.MATH_DIVISION))) {
                    result /= ((Number)parameter).doubleValue();
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        return result;
    }

    /**
     * Check the number of parameter before call the specific function.
     * @param size Parameters size to check.
     * @param parameters Original array of parameters.
     * @return Return the same original array of parameters.
     * @throws IllegalArgumentException if the size to check is not equals to the length of original parameters array.
     */
    private Object[] checkSize(int size, Object... parameters) {
        if(parameters.length != size) {
            throw new IllegalArgumentException("Illegal parameters length");
        }
        return parameters;
    }
}
