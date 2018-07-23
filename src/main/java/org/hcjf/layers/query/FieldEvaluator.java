package org.hcjf.layers.query;

import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceSession;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

/**
 * This abstract class define the structure of the evaluating. The evaluator
 * is the implementation of a method to decide if an object is part of the result
 * of the query or not is.
 * @author javaito
 */
public abstract class FieldEvaluator implements Evaluator {

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
            result = ((Query.QueryField)getLeftValue()).getFieldName().equals(fieldName);
        }
        if(!result && getRightValue() instanceof Query.QueryField) {
            result = ((Query.QueryField)getRightValue()).getFieldName().equals(fieldName);
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
            result = cache.get(this);
            if (result == null) {
                result = getProcessedValue(currentResultSetElement, getLeftValue(), dataSource, consumer);
                cache.put(this, result);
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
            result = cache.get(this);
            if (result == null) {
                result = getProcessedValue(currentResultSetElement, getRightValue(), dataSource, consumer);
                cache.put(this, result);
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
     * Return the value to compare with the field's object of the data collection's
     * instance.
     * @param currentResultSetElement Is the result set element to evaluate.
     * @param dataSource Query associated data source.
     * @param consumer Query associated data consumer.
     * @return Object value.
     */
    private final Object getProcessedValue(Object currentResultSetElement, Object rawValue, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        Object result = rawValue;

        if(result instanceof UnprocessedValue) {
            result = ((UnprocessedValue)result).process(dataSource, consumer);
        } else if(result instanceof Query.QueryParameter) {
            result = consumer.get(currentResultSetElement, (Query.QueryParameter) result, dataSource);
        } else if(result instanceof Collection) {
            Collection<Object> collectionResult = new ArrayList<>();
            for(Object internalValue : (Collection)result) {
                collectionResult.add(getProcessedValue(currentResultSetElement, internalValue, dataSource, consumer));
            }
            result = collectionResult;
        } else if(result.getClass().isArray()) {
            Collection<Object> collectionResult = new ArrayList<>();
            for (int i = 0; i < Array.getLength(result); i++) {
                collectionResult.add(getProcessedValue(currentResultSetElement, Array.get(result, i), dataSource, consumer));
            }
            result = collectionResult;
        }

        return result;
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

    /**
     * This kind of values take the true value in the execution time of the query.
     */
    public interface UnprocessedValue {

        /**
         * Return the processed value.
         * @param dataSource Data source of the in-evaluation object.
         * @param consumer Consumer for the object.
         * @return Processed value.
         */
        Object process(Queryable.DataSource dataSource, Queryable.Consumer consumer);

    }

    /**
     * Return the object that is in the specific position into the parameters array.
     */
    public static class ReplaceableValue implements UnprocessedValue {

        private final Integer place;

        public ReplaceableValue(Integer place) {
            this.place = place;
        }

        /**
         * Return the processed value.
         * @param dataSource Data source of the in-evaluation object.
         * @param consumer Consumer for the object.
         * @return Processed value.
         */
        @Override
        public Object process(Queryable.DataSource dataSource, Queryable.Consumer consumer) {
            return consumer.getParameter(place);
        }
    }

    /**
     * This kind of query value represents a sub-query.
     */
    public static class QueryValue implements UnprocessedValue {

        private final Query query;

        public QueryValue(Query query) {
            this.query = query;
        }

        /**
         * Return the sub-query instance.
         * @return Sub-query instance.
         */
        public Query getQuery() {
            return query;
        }

        /**
         * Evaluate the sub-query a return the collection result set as value.
         * The first value of the parameters array (parameters[0]) is the instance of data source to evaluate the sub-query.
         * The second value of the parameters array (parameters[1]) is the instance of the consumer to evaluate the sub-query.
         * The rest of the parameters are the parameter to evaluate the sub-query..
         * @param dataSource Data source of the in-evaluation object.
         * @param consumer Consumer for the object.
         * @return If the return fields size is one then the result will be a a list of values, else if the return fields
         * size is greater than one then the result will be a collection with object instance.
         */
        @Override
        public Object process(Queryable.DataSource dataSource, Queryable.Consumer consumer) {
            Object result;
            Collection<Object> collection;
            Collection<Object> subQueryResult = query.evaluate(dataSource, consumer);
            if(query.getReturnParameters().size() == 1){
                List<Object> listResult = new ArrayList<>();
                for(Object element : subQueryResult) {
                    listResult.add(consumer.get(element, (Query.QueryParameter) query.getReturnParameters().get(0), dataSource));
                }
                collection = listResult;
            } else {
                collection = subQueryResult;
            }

            if(collection.size() == 0) {
                //If the size of the collection result is zero then the result will be null.
                result = null;
            } else if(collection.size() == 1) {
                //If the size of the collection result is one then the result will be the unique instance of the collection.
                result = collection.iterator().next();
            } else {
                //If the size of the collection result is greater than one then the result is the collection.
                result = collection;
            }

            return result;
        }
    }
}
