package org.hcjf.layers.query;

import java.util.*;

/**
 * This class contains a query instance to evaluate using the parameters
 * associated to this instance.
 * @author javaito
 */
public class ParameterizedQuery {

    private final Query query;
    private final List<Object> parameters;

    public ParameterizedQuery(Query query) {
        this.query = query;
        this.parameters = new ArrayList<>();
    }

    /**
     * Add new parameter into the next place.
     * @param parameter Parameter to add.
     * @return Returns this instance.
     */
    public final ParameterizedQuery add(Object parameter) {
        parameters.add(parameter);
        return this;
    }

    /**
     * Add a new parameter in the specific place, if exists a parameter in the
     * indicated place then this parameter is replaced.
     * @param place Place to add the parameter.
     * @param parameter Parameter to add.
     * @return Returns this instance.
     */
    public final ParameterizedQuery set(Integer place, Object parameter) {
        parameters.set(place, parameter);
        return this;
    }

    /**
     * Returns the query associated to the instance.
     * @return Query instance.
     */
    public Query getQuery() {
        return query;
    }

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
    public final <O extends Object> Set<O> evaluate(Collection<O> dataSource) {
        return evaluate((query) -> dataSource);
    }

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
    public final <O extends Object> Set<O> evaluate(Query.DataSource<O> dataSource) {
        Set<O> result = query.evaluate(dataSource, new ParameterizedIntrospectionConsumer());
        parameters.clear();
        return result;
    }

    /**
     * This implementation use the parameters of the instance.
     */
    private class ParameterizedIntrospectionConsumer extends Query.IntrospectionConsumer {

        /**
         * Returns the parameter stored into the specific place.
         * @param place Place.
         * @return Parameter instance.
         */
        @Override
        public Object getParameter(Integer place) {
            return parameters.get(place);
        }

    }
}
