package org.hcjf.layers.query.functions;

import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.MathIntrospection;

import java.util.HashSet;
import java.util.Set;

/**
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

    private final Set<String> aliases;

    public MathQueryFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) +
                SystemProperties.get(SystemProperties.Query.Function.MATH_LAYER_NAME));
        aliases = new HashSet<>();
        for(String functionName : MathIntrospection.getMethodsSet()){
            aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + functionName);
        }
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

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
            default: result = MathIntrospection.invoke(functionName, parameters);
        }
        return result;
    }

    private Object[] checkSize(int size, Object... parameters) {
        if(parameters.length != size) {
            throw new IllegalArgumentException("Illegal parameters length");
        }
        return parameters;
    }
}
