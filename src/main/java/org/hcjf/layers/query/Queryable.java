package org.hcjf.layers.query;

import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.query.evaluators.FieldEvaluator;
import org.hcjf.layers.query.functions.QueryAggregateFunctionLayerInterface;
import org.hcjf.layers.query.functions.QueryFunctionLayerInterface;
import org.hcjf.layers.query.model.QueryField;
import org.hcjf.layers.query.model.QueryFunction;
import org.hcjf.layers.query.model.QueryParameter;
import org.hcjf.layers.query.model.QueryReturnFunction;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.bson.BsonParcelable;

import java.util.*;

/**
 * @author javaito
 */
public interface Queryable extends BsonParcelable {

    /**
     * Return the resource name.
     * @return Resource name.
     */
    String getResourceName();

    /**
     * This method evaluate each object of the collection and sort filtered
     * object to create a result add with the object filtered and sorted.
     * If there are order fields added then the result implementation is a
     * {@link TreeSet} implementation else the result implementation is a
     * {@link LinkedHashSet} implementation in order to guarantee the data order
     * from the source
     * @param dataSource Data source to evaluate the query.
     * @param <O> Kind of instances of the data collection.
     * @return Result add filtered and sorted.
     */
    <O extends Object> Collection<O> evaluate(Collection<O> dataSource);

    /**
     * This method evaluate each object of the collection and sort filtered
     * object to create a result add with the object filtered and sorted.
     * If there are order fields added then the result implementation is a
     * {@link TreeSet} implementation else the result implementation is a
     * {@link LinkedHashSet} implementation in order to guarantee the data order
     * from the source
     * @param dataSource Data source to evaluate the query.
     * @param consumer Data source consumer.
     * @param <O> Kind of instances of the data collection.
     * @return Result add filtered and sorted.
     */
    <O extends Object> Collection<O> evaluate(Collection<O> dataSource, Consumer<O> consumer);

    /**
     * This method evaluate each object of the collection and sort filtered
     * object to create a result add with the object filtered and sorted.
     * If there are order fields added then the result implementation is a
     * {@link TreeSet} implementation else the result implementation is a
     * {@link LinkedHashSet} implementation in order to guarantee the data order
     * from the source
     * @param dataSource Data source to evaluate the query.
     * @param <O> Kind of instances of the data collection.
     * @return Result add filtered and sorted.
     */
    <O extends Object> Collection<O> evaluate(DataSource<O> dataSource);

    /**
     * This method evaluate each object of the collection and sort filtered
     * object to create a result add with the object filtered and sorted.
     * If there are order fields added then the result implementation is a
     * {@link TreeSet} implementation else the result implementation is a
     * {@link LinkedHashSet} implementation in order to guarantee the data order
     * from the source
     * @param dataSource Data source to evaluate the query.
     * @param consumer Data source consumer.
     * @param <O> Kind of instances of the data collection.
     * @return Result add filtered and sorted.
     */
    <O extends Object> Collection<O> evaluate(DataSource<O> dataSource, Consumer<O> consumer);

    /**
     * This class provides an interface to consume a
     * different collection of naming data to be useful in evaluation
     * process.
     */
    interface Consumer<O extends Object> {

        /**
         * Get naming information from an instance.
         * @param instance Data source.
         * @param queryParameter Query parameter.
         * @param dataSource Data source
         * @param <R> Expected response type.
         * @return Return the data storage in the data source indexed
         * by the parameter name.
         */
        <R extends Object> R get(O instance, QueryParameter queryParameter, DataSource<O> dataSource);

        /**
         * This method must resolve the functions that are used into the query object.
         * @param function Query function.
         * @param instance Data object instance.
         * @param dataSource Data source
         * @param <R> Expected result.
         * @return Return the value obtained of the function resolution.
         */
        <R extends Object> R resolveFunction(QueryFunction function, Object instance, DataSource<O> dataSource);

        /**
         * This method must returns the parameter for the place indicated as parameter.
         * @param place Place value.
         * @param <R> Expected result type.
         * @return Returns the value for the specific place.
         */
        <R extends Object> R getParameter(Integer place);

    }

    /**
     * This interface must implements a provider to obtain the data collection
     * for diferents resources.
     */
    interface DataSource<O extends Object> {

        /**
         * This method musr return the data of diferents resources using some query.
         * @param queryable Query object.
         * @return Data collection from the resource.
         */
        Collection<O> getResourceData(Queryable queryable);

    }

    abstract class DefaultConsumer <O extends Object> implements Consumer<O> {

        /**
         * This method must resolve the functions that are used into the query object.
         * @param function Query function.
         * @param <R> Expected result.
         * @return Return the value obtained of the function resolution.
         */
        @Override
        public <R extends Object> R resolveFunction(QueryFunction function, Object instance, DataSource<O> dataSource) {
            List<Object> parameterValues = new ArrayList<>();
            Object currentParameter;
            Object value;
            for (int i = 0; i < function.getParameters().size(); i++) {
                currentParameter = function.getParameters().get(i);
                if(currentParameter != null) {
                    if (currentParameter instanceof QueryFunction) {
                        if(function instanceof QueryReturnFunction && ((QueryReturnFunction)function).isAggregate()) {
                            parameterValues.add(currentParameter);
                        } else {
                            QueryFunction innerFunction = (QueryFunction) currentParameter;
                            value = resolveFunction(innerFunction, instance, dataSource);
                            parameterValues.add(value);
                        }
                    } else if (currentParameter instanceof QueryParameter) {
                        if(function instanceof QueryReturnFunction && ((QueryReturnFunction)function).isAggregate()) {
                           parameterValues.add(currentParameter);
                        } else {
                            value = get((O) instance, ((QueryParameter) currentParameter), dataSource);
                            parameterValues.add(value);
                        }
                    } else if (currentParameter instanceof FieldEvaluator.UnprocessedValue) {
                        parameterValues.add(((FieldEvaluator.UnprocessedValue)currentParameter).
                                process(dataSource, this));
                    } else {
                        parameterValues.add(currentParameter);
                    }
                }
            }

            R result;
            if(function instanceof QueryReturnFunction && ((QueryReturnFunction)function).isAggregate()) {
                QueryAggregateFunctionLayerInterface queryAggregateFunctionLayerInterface = Layers.get(QueryAggregateFunctionLayerInterface.class,
                        SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + function.getFunctionName());
                String alias = ((QueryReturnFunction) function).getAlias() == null ? function.toString() : ((QueryReturnFunction) function).getAlias();
                result = (R) queryAggregateFunctionLayerInterface.evaluate(alias, (Collection) instance, parameterValues.toArray());
            } else {
                QueryFunctionLayerInterface queryFunctionLayerInterface = Layers.get(QueryFunctionLayerInterface.class,
                        SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + function.getFunctionName());
                result = (R) queryFunctionLayerInterface.evaluate(function.getFunctionName(), parameterValues.toArray());
            }
            return result;
        }

    }

    /**
     * This private class is the default consume method of the queries.
     */
    class IntrospectionConsumer<O extends Object> extends DefaultConsumer<O> {

        /**
         * Get naming information from an instance.
         *
         * @param instance    Data source.
         * @param queryParameter Query parameter.
         * @return Return the data storage in the data source indexed
         * by the parameter name.
         */
        @Override
        public <R extends Object> R get(O instance, QueryParameter queryParameter, DataSource<O> dataSource) {
            Object result = null;
            if(queryParameter instanceof QueryField) {
                QueryField queryField = (QueryField) queryParameter;
                if(queryField.getFieldPath().equals(SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL))) {
                    result = SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL);
                } else {
                    result = queryField.resolve(instance);
                }
            } else if(queryParameter instanceof QueryFunction) {
                result = resolveFunction((QueryFunction) queryParameter, instance, dataSource);
            }
            return (R) result;
        }

        @Override
        public <R> R getParameter(Integer place) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * This data source find all the resources that implements {@link ReadRowsLayerInterface} interface
     */
    class ReadableDataSource implements DataSource<JoinableMap> {

        /**
         * Return the collection of data as query response.
         * @param queryable Query object.
         * @return Collection of data.
         */
        @Override
        public Collection<JoinableMap> getResourceData(Queryable queryable) {
            return Layers.get(ReadRowsLayerInterface.class, queryable.getResourceName()).readRows(queryable);
        }

    }
}
