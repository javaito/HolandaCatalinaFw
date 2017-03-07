package org.hcjf.layers.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This abstract class define the structure of the evaluating. The evaluator
 * is the implementation of a method to decide if an object is part of the result
 * of the query or not is.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class FieldEvaluator implements Evaluator {

    private final Query.QueryParameter queryParameter;
    private final Object value;

    public FieldEvaluator(Query.QueryParameter queryParameter, Object value) {
        this.queryParameter = queryParameter;
        this.value = value;
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
            result = this.queryParameter.equals(fieldEvaluator.queryParameter) &&
                    this.value.equals(fieldEvaluator.value);
        }

        return result;
    }

    /**
     * Return the query field associated to the evaluator.
     * @return Query field.
     */
    public Query.QueryParameter getQueryParameter() {
        return queryParameter;
    }

    /**
     * Return the value to compare with the field's object of the data collection's
     * instance.
     * @return Object value.
     */
    public final Object getValue(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters) {
        Object result = value;
        if(result instanceof UnprocessedValue) {
            result = ((UnprocessedValue)value).process(object, dataSource, consumer, parameters);
        }

        if(result instanceof Collection) {
            Collection<Object> collectionResult = new ArrayList<>();
            for(Object internalValue : (Collection)result) {
                if(internalValue instanceof UnprocessedValue) {
                    collectionResult.add(((UnprocessedValue)internalValue).process(object, dataSource, consumer, parameters));
                } else {
                    collectionResult.add(internalValue);
                }
            }
            result = collectionResult;
        }

        return result;
    }

    /**
     * Return the raw value container into the evaluator.
     * @return Raw value.
     */
    public final Object getRawValue() {
        return value;
    }

    /**
     * Return the class of the original value.
     * @return Class of the original value.
     */
    public final Class getValueType() {
        return value.getClass();
    }

    /**
     * Return the string representation of the evaluator.
     * @return Format: ClassName[fieldName,value]
     */
    @Override
    public String toString() {
        return getClass() + "[" + queryParameter + "," + value + "]";
    }

    /**
     * This kind of values take the true value in the execution time of the query.
     */
    public interface UnprocessedValue {

        /**
         * Return the processed value.
         * @param object In-evaluation object.
         * @param dataSource Data source of the in-evaluation object.
         * @param consumer Consumer for the object.
         * @param parameters Implementation parameters.
         * @return Processed value.
         */
        public Object process(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters);

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
         * @param object In-evaluation object.
         * @param dataSource Data source of the in-evaluation object.
         * @param consumer Consumer for the object.
         * @param parameters Implementation parameters.
         * @return Processed value.
         */
        @Override
        public Object process(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters) {
            if(parameters.length <= place) {
                throw new IllegalArgumentException("Non-specified replaceable value, index " + place);
            }

            return parameters[place];
        }
    }

    /**
     * This implementation is for the case that the evaluator use 2 fields of the
     * same row.
     */
    public static class QueryFieldValue implements UnprocessedValue {

        private final Query.QueryParameter queryParameter;

        public QueryFieldValue(Query.QueryParameter queryParameter) {
            this.queryParameter = queryParameter;
        }

        /**
         * Return the value of the other field.
         * @param object In-evaluation object.
         * @param dataSource Data source of the in-evaluation object.
         * @param consumer Consumer for the object.
         * @param parameters Implementation parameters.
         * @return Field value.
         */
        @Override
        public Object process(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters) {
            return consumer.get(object, queryParameter);
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
         * The rest of the parameters are the parameter to evaluate the sub-query.
         * @param object In-evaluation object.
         * @param dataSource Data source of the in-evaluation object.
         * @param consumer Consumer for the object.
         * @param parameters Implementation parameters.
         * @return If the return fields size is one then the result will be a a list of values, else if the return fields
         * size is greater than one then the result will be a collection with object instance.
         */
        @Override
        public Object process(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters) {
            Object result;
            Collection<Object> collection;
            Collection<Object> subQueryResult = query.evaluate(dataSource, consumer, parameters);
            if(query.getReturnParameters().size() == 1){
                List<Object> listResult = new ArrayList<>();
                for(Object element : subQueryResult) {
                    listResult.add(consumer.get(element, (Query.QueryParameter) query.getReturnParameters().get(0)));
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
