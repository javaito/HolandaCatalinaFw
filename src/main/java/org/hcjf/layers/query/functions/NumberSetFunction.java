package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.BiFunction;

public interface NumberSetFunction {

    /**
     * This method apply a function with two parameters, the first parameter is the accumulated value and the second
     * parameter is the new value to apply.
     * @param accumulatedValue Initial value of the accumulated function.
     * @param parameters Parameters function.
     * @param function Function to apply.
     * @return Returns the account of values the function apply.
     */
    default Number[] accumulateFunction(Number accumulatedValue, Object[] parameters, BiFunction<BigDecimal, BigDecimal, Number> function) {
        Integer counter = 0;
        for(Object numericParameter : parameters) {
            if(numericParameter instanceof Collection) {
                for(Number collectionNumber : ((Collection<Number>)numericParameter)) {
                    accumulatedValue = applyFunction(accumulatedValue, collectionNumber, function);
                    counter++;
                }
            } else {
                accumulatedValue = applyFunction(accumulatedValue, ((Number) numericParameter), function);
                counter++;
            }
        }
        return new Number[] {counter, accumulatedValue};
    }

    default Number applyFunction(Number firstNumber, Number secondNumber, BiFunction<BigDecimal, BigDecimal, Number> function) {
        Number result = 0;
        Class biggerClass = getBiggerClass(firstNumber, secondNumber);
        if(biggerClass.equals(Byte.class)) {
            result = function.apply(new BigDecimal(firstNumber.byteValue()), new BigDecimal(secondNumber.byteValue()));
        } else if(biggerClass.equals(Short.class)) {
            result = function.apply(new BigDecimal(firstNumber.shortValue()), new BigDecimal(secondNumber.shortValue()));
        } else if(biggerClass.equals(Integer.class)) {
            result = function.apply(new BigDecimal(firstNumber.intValue()), new BigDecimal(secondNumber.intValue()));
        } else if(biggerClass.equals(Long.class)) {
            result = function.apply(new BigDecimal(firstNumber.longValue()), new BigDecimal(secondNumber.longValue()));
        } else if(biggerClass.equals(Float.class)) {
            result = function.apply(new BigDecimal(firstNumber.floatValue()), new BigDecimal(secondNumber.floatValue()));
        } else if(biggerClass.equals(Double.class)) {
            result = function.apply(new BigDecimal(firstNumber.doubleValue()), new BigDecimal(secondNumber.doubleValue()));
        }
        return result;
    }

    /**
     * This method iterate all the parameter to evaluate math expression. Each parameter contains
     * a number value or operator.
     * @param parameters Math expression parameters.
     * @return Returns the number that results of evaluate the math expression.
     */
    default Number evalExpression(Object... parameters) {
        Number result = 0;
        Number secondParameter;
        String currentOperation = SystemProperties.get(SystemProperties.Query.Function.MATH_ADDITION);
        for(Object parameter : parameters) {
            if(parameter instanceof String) {
                currentOperation = (String) parameter;
            } else if(parameter instanceof Number) {
                secondParameter = (Number) parameter;
                if(currentOperation.equals(SystemProperties.get(SystemProperties.Query.Function.MATH_ADDITION))) {
                    result = applyFunction(result, secondParameter, BigDecimal::add);
                } else if(currentOperation.equals(SystemProperties.get(SystemProperties.Query.Function.MATH_SUBTRACTION))) {
                    result = applyFunction(result, secondParameter, BigDecimal::subtract);
                } else if(currentOperation.equals(SystemProperties.get(SystemProperties.Query.Function.MATH_MULTIPLICATION))) {
                    result = applyFunction(result, secondParameter, BigDecimal::multiply);
                } else if(currentOperation.equals(SystemProperties.get(SystemProperties.Query.Function.MATH_DIVISION))) {
                    result = applyFunction(result, secondParameter, BigDecimal::divide);
                } else if(currentOperation.equals(SystemProperties.get(SystemProperties.Query.Function.MATH_MODULUS))) {
                    result = applyFunction(result, secondParameter, BigDecimal::remainder);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        return result;
    }

    default Class getBiggerClass(Number firstNumber, Number secondNumber) {
        Class result;
        if(firstNumber instanceof Double || secondNumber instanceof Double) {
            result = Double.class;
        } else if(firstNumber instanceof Float || secondNumber instanceof Float) {
            result = Float.class;
        } else if(firstNumber instanceof Long || secondNumber instanceof Long) {
            result = Long.class;
        } else if(firstNumber instanceof Integer || secondNumber instanceof Integer) {
            result = Integer.class;
        } else if(firstNumber instanceof Short || secondNumber instanceof Short) {
            result = Short.class;
        } else if(firstNumber instanceof Byte || secondNumber instanceof Byte) {
            result = Byte.class;
        } else {
            result = Double.class;
        }
        return result;
    }

}
