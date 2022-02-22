package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.MathIntrospection;
import org.hcjf.utils.Maths;
import org.hcjf.utils.Matrix;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
    private static final String NUMBER_FORMAT = "numberFormat";
    private static final String CURRENCY_FORMAT = "currencyFormat";
    private static final String PERCENT_FORMAT = "percentFormat";
    private static final String PARSE_NUMBER = "parseNumber";
    private static final String NEW_MATRIX = "newMatrix";
    private static final String NEW_SUB_MATRIX = "newSubMatrix";
    private static final String NEW_IDENTITY_MATRIX = "newIdentityMatrix";
    private static final String IS_SQUARE_MATRIX = "isSquareMatrix";
    private static final String MATRIX_GET = "matrixGet";
    private static final String MATRIX_SET = "matrixSet";
    private static final String MATRIX_ADD = "matrixAdd";
    private static final String MATRIX_SUBTRACT = "matrixSubtract";
    private static final String MATRIX_MULTIPLY = "matrixMultiply";
    private static final String MATRIX_MULTIPLY_BY_SCALAR = "matrixMultiplyByScalar";
    private static final String MATRIX_TRANSPOSE = "matrixTranspose";
    private static final String MATRIX_DETERMINANT = "matrixDeterminant";
    private static final String MATRIX_COFACTOR = "matrixCofactor";
    private static final String MATRIX_INVERSE = "matrixInverse";
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
        addFunctionName(NUMBER_FORMAT);
        addFunctionName(CURRENCY_FORMAT);
        addFunctionName(PERCENT_FORMAT);
        addFunctionName(PARSE_NUMBER);
        addFunctionName(EVAL_EXPRESSION);
        addFunctionName(NEW_MATRIX);
        addFunctionName(NEW_SUB_MATRIX);
        addFunctionName(NEW_IDENTITY_MATRIX);
        addFunctionName(IS_SQUARE_MATRIX);
        addFunctionName(MATRIX_GET);
        addFunctionName(MATRIX_SET);
        addFunctionName(MATRIX_ADD);
        addFunctionName(MATRIX_SUBTRACT);
        addFunctionName(MATRIX_MULTIPLY);
        addFunctionName(MATRIX_MULTIPLY_BY_SCALAR);
        addFunctionName(MATRIX_TRANSPOSE);
        addFunctionName(MATRIX_DETERMINANT);
        addFunctionName(MATRIX_COFACTOR);
        addFunctionName(MATRIX_INVERSE);
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
            case BYTE_VALUE: result = ((Number)getParameter(0, parameters)).byteValue(); break;
            case SHORT_VALUE: result = ((Number)getParameter(0, parameters)).shortValue(); break;
            case INTEGER_VALUE: result = ((Number)getParameter(0, parameters)).intValue(); break;
            case LONG_VALUE: result = ((Number)getParameter(0, parameters)).longValue(); break;
            case FLOAT_VALUE: result = ((Number)getParameter(0, parameters)).floatValue(); break;
            case DOUBLE_VALUE: result = ((Number)getParameter(0, parameters)).doubleValue(); break;
            case NUMBER_FORMAT: {
                String pattern = getParameter(0, parameters);
                Number number = getParameter(1, parameters);
                NumberFormat numberFormat = new DecimalFormat(pattern);
                result = numberFormat.format(number);
                break;
            }
            case CURRENCY_FORMAT: {
                Number number = getParameter(0, parameters);
                Locale locale = ServiceSession.getCurrentIdentity().getLocale();
                result = NumberFormat.getCurrencyInstance(locale).format(number.doubleValue());
                break;
            }
            case PERCENT_FORMAT: {
                Number number = getParameter(0, parameters);
                Locale locale = ServiceSession.getCurrentIdentity().getLocale();
                result = NumberFormat.getPercentInstance(locale).format(number.doubleValue());
                break;
            }
            case PARSE_NUMBER: {
                String pattern = getParameter(0, parameters);
                String source = getParameter(1, parameters);
                NumberFormat numberFormat = new DecimalFormat(pattern);
                try {
                    result = numberFormat.parse(source);
                } catch (ParseException ex) {
                    throw new HCJFRuntimeException("Number parse fail", ex);
                }
                break;
            }
            case NEW_MATRIX: {
                Number rows = getParameter(0, parameters);
                Number cols = getParameter(1, parameters);
                if(parameters.length >= 3) {
                    List<Number> values = getParameter(2, parameters);
                    result = new Matrix(rows.intValue(), cols.intValue(), values.toArray(new Number[]{}));
                } else {
                    result = new Matrix(rows.intValue(), cols.intValue());
                }
                break;
            }
            case NEW_IDENTITY_MATRIX: {
                Number size = getParameter(0, parameters);
                result = Matrix.identity(size.intValue());
                break;
            }
            case NEW_SUB_MATRIX: {
                Matrix matrix = getParameter(0, parameters);
                Number excludeRow = getParameter(1, parameters);
                Number excludeColumn = getParameter(2, parameters);
                result = Maths.createSubMatrix(matrix, excludeRow.intValue(), excludeColumn.intValue());
                break;
            }
            case IS_SQUARE_MATRIX: {
                Matrix matrix = getParameter(0, parameters);
                result = matrix.isSquare();
                break;
            }
            case MATRIX_GET: {
                Matrix matrix = getParameter(0, parameters);
                Number row = getParameter(1, parameters);
                Number col = getParameter(2, parameters);
                result = matrix.get(row.intValue(), col.intValue());
                break;
            }
            case MATRIX_SET: {
                Matrix matrix = getParameter(0, parameters);
                Number row = getParameter(1, parameters);
                Number col = getParameter(2, parameters);
                Number value = getParameter(3, parameters);
                matrix.set(row.intValue(), col.intValue(), value);
                result = matrix;
                break;
            }
            case MATRIX_ADD: {
                Matrix matrixA = getParameter(0, parameters);
                Matrix matrixB = getParameter(1, parameters);
                result = Maths.matrixAdd(matrixA, matrixB);
                break;
            }
            case MATRIX_SUBTRACT: {
                Matrix matrixA = getParameter(0, parameters);
                Matrix matrixB = getParameter(1, parameters);
                result = Maths.matrixSubtract(matrixA, matrixB);
                break;
            }
            case MATRIX_MULTIPLY: {
                Matrix matrixA = getParameter(0, parameters);
                Matrix matrixB = getParameter(1, parameters);
                result = Maths.matrixMultiply(matrixA, matrixB);
                break;
            }
            case MATRIX_MULTIPLY_BY_SCALAR: {
                Matrix matrix = getParameter(0, parameters);
                Number scalar = getParameter(1, parameters);
                result = Maths.matrixMultiplyByScalar(matrix, scalar);
                break;
            }
            case MATRIX_TRANSPOSE: {
                Matrix matrix = getParameter(0, parameters);
                result = Maths.matrixTranspose(matrix);
                break;
            }
            case MATRIX_COFACTOR: {
                Matrix matrix = getParameter(0, parameters);
                result = Maths.matrixCofactor(matrix);
                break;
            }
            case MATRIX_DETERMINANT: {
                Matrix matrix = getParameter(0, parameters);
                result = Maths.matrixDeterminant(matrix);
                break;
            }
            case MATRIX_INVERSE: {
                Matrix matrix = getParameter(0, parameters);
                result = Maths.matrixInverse(matrix);
                break;
            }
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
