package org.hcjf.layers.query;

import org.hcjf.bson.BsonDocument;
import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.utils.bson.BsonCustomBuilderLayer;
import org.hcjf.utils.bson.BsonParcelable;

import java.util.*;

/**
 * This class contains a query instance to evaluate using the parameters
 * associated to this instance.
 * @author javaito
 */
public class ParameterizedQuery implements Queryable {

    private static final String QUERY_BSON_FIELD_NAME = "__query__";
    private static final String PARAMS_BSON_FIELD_NAME = "__params__";

    static {
        Layers.publishLayer(ParameterizedQueryBsonCustomBuilderLayer.class);
    }

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
    @Override
    public final <O extends Object> Set<O> evaluate(Collection<O> dataSource) {
        return evaluate((query) -> dataSource, new Queryable.IntrospectionConsumer<>());
    }

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
    @Override
    public final <O extends Object> Set<O> evaluate(Collection<O> dataSource, Queryable.Consumer<O> consumer) {
        return evaluate((query) -> dataSource, consumer);
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
    @Override
    public final <O extends Object> Set<O> evaluate(Queryable.DataSource<O> dataSource) {
        return evaluate(dataSource, new Queryable.IntrospectionConsumer<>());
    }

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
    @Override
    public final <O extends Object> Set<O> evaluate(Queryable.DataSource<O> dataSource, Queryable.Consumer<O> consumer) {
        Set<O> result = query.evaluate(dataSource, new ParameterizedConsumer(consumer));
        parameters.clear();
        return result;
    }

    @Override
    public BsonDocument toBson() {
        BsonDocument document = new BsonDocument();
        document.put(PARCELABLE_CLASS_NAME, getClass().getName());
        document.put(QUERY_BSON_FIELD_NAME, query.toString());
        document.put(PARAMS_BSON_FIELD_NAME, parameters);
        return document;
    }

    @Override
    public <P extends BsonParcelable> P populate(BsonDocument document) {
        Collection collection = fromBson(Object.class, document.get(PARAMS_BSON_FIELD_NAME).getAsArray());
        parameters.addAll(collection);
        return (P) this;
    }

    /**
     * This implementation use the parameters of the instance.
     */
    private class ParameterizedConsumer extends Queryable.DefaultConsumer {

        private final Consumer consumer;

        public ParameterizedConsumer(Consumer consumer) {
            this.consumer = consumer;
        }

        /**
         * Call the implementation of the inner consumer instance.
         * @param instance Data source.
         * @param queryParameter Query parameter.
         * @return Returns the value of the inner consumer.
         */
        @Override
        public Object get(Object instance, Query.QueryParameter queryParameter, DataSource dataSource) {
            return consumer.get(instance, queryParameter, dataSource);
        }

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

    /**
     * This inner class implements the custom method to create a Parameterized Query instance from a bson document.
     */
    public static class ParameterizedQueryBsonCustomBuilderLayer extends Layer
            implements BsonCustomBuilderLayer<ParameterizedQuery> {

        public ParameterizedQueryBsonCustomBuilderLayer() {
            super(ParameterizedQuery.class.getName());
        }

        /**
         * This implementation required that the document contains a field called '__query__'
         * and the field called '__params__' to create the parameterized query instance.
         * @param document Bson document.
         * @return Parameterized query instance.
         */
        @Override
        public ParameterizedQuery create(BsonDocument document) {
            return new ParameterizedQuery(Query.compile(document.get(QUERY_BSON_FIELD_NAME).getAsString()));
        }

    }
}
