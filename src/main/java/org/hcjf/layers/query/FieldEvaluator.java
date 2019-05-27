package org.hcjf.layers.query;

import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceSession;

import java.math.BigDecimal;
import java.util.Map;

/**
 * This abstract class define the structure of the evaluating. The evaluator
 * is the implementation of a method to decide if an object is part of the result
 * of the query or not is.
 * @author javaito
 */
public abstract class FieldEvaluator extends BaseEvaluator {

    private final Object leftValue;
    private final Object rightValue;

    public FieldEvaluator(Object leftValue, Object rightValue) {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    /**
     * This method check if the evaluator contains a reference of the field indicated as parameter.
     * @param fieldName Field name.
     * @return True if the evaluator contains the reference and false in the otherwise.
     */
    public final boolean containsReference(String fieldName) {
        boolean result = false;
        if(getLeftValue() instanceof Query.QueryField) {
            result = ((Query.QueryField)getLeftValue()).getFieldPath().equals(fieldName);
        }
        if(!result && getRightValue() instanceof Query.QueryField) {
            result = ((Query.QueryField)getRightValue()).getFieldPath().equals(fieldName);
        }
        return result;
    }

    /**
     * Returns the left value of the evaluator.
     * @return Left value of the evaluator.
     */
    public final Object getLeftValue() {
        return leftValue;
    }

    /**
     * Returns the left processed value for the specific data source and consumer.
     * @param currentResultSetElement Is the result set element to evaluate.
     * @param dataSource Data source instance.
     * @param consumer Consumer instance.
     * @return Processed left value.
     */
    protected final Object getProcessedLeftValue(Object currentResultSetElement, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        Object result;
        if(getLeftValue() instanceof Query.QueryParameter) {
            result = getProcessedValue(currentResultSetElement, getLeftValue(), dataSource, consumer);
        } else {
            Map<Evaluator, Object> cache = getLeftCache();
            if(cache != null) {
                result = cache.get(this);
                if (result == null) {
                    result = getProcessedValue(currentResultSetElement, getLeftValue(), dataSource, consumer);
                    cache.put(this, result);
                }
            } else {
                result = getProcessedValue(currentResultSetElement, getLeftValue(), dataSource, consumer);
            }
        }
        return result;
    }

    /**
     * Returns the map that contains the processed left values for each evaluator.
     * @return Cache instance
     */
    private final Map<Evaluator,Object> getLeftCache() {
        return ServiceSession.getCurrentIdentity().get(SystemProperties.get(SystemProperties.Query.EVALUATOR_LEFT_VALUES_CACHE_NAME));
    }

    /**
     * Returns the right value of the evaluator.
     * @return Right value of the evaluator.
     */
    public final Object getRightValue() {
        return rightValue;
    }

    /**
     * Returns the right processed value for the specific data source and consumer.
     * @param currentResultSetElement Is the result set element to evaluate.
     * @param dataSource Data source instance.
     * @param consumer Consumer instance.
     * @return Processed right value.
     */
    protected final Object getProcessedRightValue(Object currentResultSetElement, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        Object result;
        if(getRightValue() instanceof Query.QueryParameter) {
            result = getProcessedValue(currentResultSetElement, getRightValue(), dataSource, consumer);
        } else {
            Map<Evaluator, Object> cache = getRightCache();
            if(cache != null) {
                result = cache.get(this);
                if (result == null) {
                    result = getProcessedValue(currentResultSetElement, getRightValue(), dataSource, consumer);
                    cache.put(this, result);
                }
            } else {
                result = getProcessedValue(currentResultSetElement, getRightValue(), dataSource, consumer);
            }
        }
        return result;
    }

    /**
     * Returns the map that contains the processed right values for each evaluator.
     * @return Cache instance
     */
    private Map<Evaluator,Object> getRightCache() {
        return ServiceSession.getCurrentIdentity().get(SystemProperties.get(SystemProperties.Query.EVALUATOR_RIGHT_VALUES_CACHE_NAME));
    }

    /**
     * Copy this field evaluator with other value.
     * @return New instance.
     */
    public final FieldEvaluator copy() {
        try {
            return getClass().getConstructor(Object.class, Object.class).
                    newInstance(leftValue, rightValue);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }

    /**
     * Normalize any kind of number and compare both;
     * @param fieldValue Field value.
     * @param value Value.
     * @return True if the field value and value are equals as number.
     */
    protected boolean numberEquals(Number fieldValue, Object value) {
        boolean result = false;
        if(value instanceof Number) {
            if(fieldValue instanceof Double || fieldValue instanceof Float ||
                    value instanceof Double || value instanceof Float) {
                result = new BigDecimal(fieldValue.doubleValue()).equals(
                        new BigDecimal(((Number) value).doubleValue()));
            } else {
                result = fieldValue.longValue() == ((Number) value).longValue();
            }
        }
        return result;
    }

    /**
     * Two evaluators are equals when are instances of the same class,
     * his field names are equals and his values are equals
     * @param obj Object to compare.
     * @return True if the instance is equals than object parameter and
     * false in the other ways.
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if(obj.getClass().equals(getClass())) {
            FieldEvaluator fieldEvaluator = (FieldEvaluator) obj;
            result = this.leftValue.equals(fieldEvaluator.leftValue) &&
                    this.rightValue.equals(fieldEvaluator.rightValue);
        }

        return result;
    }

    /**
     * Return the string representation of the evaluator.
     * @return Format: ClassName[fieldName,value]
     */
    @Override
    public String toString() {
        return getClass() + "[" + leftValue + "," + rightValue + "]";
    }

}
