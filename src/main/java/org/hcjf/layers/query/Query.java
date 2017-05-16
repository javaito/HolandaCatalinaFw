package org.hcjf.layers.query;

import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.CrudLayerInterface;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.Strings;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class contains all the parameter needed to create a query.
 * This kind of queries works over any data collection.
 * @author javaito
 *
 */
public class Query extends EvaluatorCollection {

    private final QueryId id;
    private final QueryResource resource;
    private Integer limit;
    private Integer start;
    private final List<QueryReturnField> groupParameters;
    private final List<QueryOrderParameter> orderParameters;
    private final List<QueryReturnParameter> returnParameters;
    private final List<Join> joins;

    public Query(String resource, QueryId id) {
        this.id = id;
        this.groupParameters = new ArrayList<>();
        this.orderParameters = new ArrayList<>();
        this.returnParameters = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.resource = new QueryResource(resource);
    }

    public Query(String resource){
        this(resource, new QueryId());
    }

    private Query(Query source) {
        super(source);
        this.id = new QueryId();
        this.resource = source.resource;
        this.limit = source.limit;
        this.start = source.start;
        this.orderParameters = new ArrayList<>();
        this.orderParameters.addAll(source.orderParameters);
        this.returnParameters = new ArrayList<>();
        this.returnParameters.addAll(source.returnParameters);
        this.groupParameters = new ArrayList<>();
        this.groupParameters.addAll(source.groupParameters);
        this.joins = new ArrayList<>();
        this.joins.addAll(source.joins);
    }

    private QueryParameter checkQueryParameter(QueryParameter queryParameter) {
        if(queryParameter instanceof QueryField) {
            QueryField queryField = (QueryField) queryParameter;
            QueryResource resource = queryField.getResource();
            if (resource == null) {
                queryField.setResource(getResource());
            }
        } else if(queryParameter instanceof QueryFunction) {
            QueryFunction function = (QueryFunction) queryParameter;
            for(Object functionParameter : function.getParameters()) {
                if(functionParameter instanceof QueryParameter) {
                    checkQueryParameter((QueryParameter) functionParameter);
                }
            }
        }
        return queryParameter;
    }

    @Override
    protected Evaluator checkEvaluator(Evaluator evaluator) {
        if(evaluator instanceof FieldEvaluator) {
            checkQueryParameter(((FieldEvaluator)evaluator).getQueryParameter());
        }
        return evaluator;
    }

    /**
     * Return the id of the query.
     * @return Id of the query.
     */
    public final QueryId getId() {
        return id;
    }

    /**
     * Return the list of joins.
     * @return Joins.
     */
    public List<Join> getJoins() {
        return Collections.unmodifiableList(joins);
    }

    /**
     * Return the resource query object.
     * @return Resource query.
     */
    public QueryResource getResource() {
        return resource;
    }

    /**
     * Return the resource name.
     * @return Resource name.
     */
    public final String getResourceName() {
        return resource.getResourceName();
    }

    /**
     * Return the limit of the query.
     * @return Query limit.
     */
    public final Integer getLimit() {
        return limit;
    }

    /**
     * Set the query limit.
     * @param limit Query limit.
     */
    public final void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * Return the object that represents the first element of the result.
     * @return Firts object of the result.
     */
    public final Integer getStart() {
        return start;
    }

    /**
     * Set the first object of the result.
     * @param start First object of the result.
     */
    public final void setStart(Integer start) {
        this.start = start;
    }

    /**
     * Return all the group fields of the query.
     * @return Group field of the query.
     */
    public List<QueryField> getGroupParameters() {
        return Collections.unmodifiableList(groupParameters);
    }

    /**
     * Add a name of the field for group the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param groupField Name of the pair getter/setter.
     * @return Return the same instance of this class.
     */
    public final Query addGroupField(String groupField) {
        return addGroupField(new QueryReturnField(groupField));
    }

    /**
     * Add a name of the field for group the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param groupField Name of the pair getter/setter.
     * @return Return the same instance of this class.
     */
    public final Query addGroupField(QueryReturnField groupField) {
        groupParameters.add((QueryReturnField) checkQueryParameter(groupField));
        return this;
    }

    /**
     * Return the unmodifiable list with order fields.
     * @return Order fields.
     */
    public final List<QueryOrderParameter> getOrderParameters() {
        return Collections.unmodifiableList(orderParameters);
    }

    /**
     * Add a name of the field for order the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param orderField Name of the pair getter/setter.
     * @return Return the same instance of this class.
     */
    public final Query addOrderField(String orderField) {
        return addOrderField(orderField, SystemProperties.getBoolean(SystemProperties.Query.DEFAULT_DESC_ORDER));
    }

    /**
     * Add a name of the field for order the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param orderField Name of the pair getter/setter.
     * @param desc Desc property.
     * @return Return the same instance of this class.
     */
    public final Query addOrderField(String orderField, boolean desc) {
        return addOrderField(new QueryOrderField(orderField, desc));
    }

    /**
     * Add a name of the field for order the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param orderParameter Order parameter.
     * @return Return the same instance of this class.
     */
    public final Query addOrderField(QueryOrderParameter orderParameter) {
        orderParameters.add((QueryOrderParameter) checkQueryParameter((QueryParameter) orderParameter));
        return this;
    }

    /**
     * Return an unmodifiable list with the return fields.
     * @return Return fields.
     */
    public final List<QueryReturnParameter> getReturnParameters() {
        return Collections.unmodifiableList(returnParameters);
    }

    /**
     * Add the name of the field to be returned in the result set.
     * @param returnField Field name.
     * @return Return the same instance of this class.
     */
    public final Query addReturnField(String returnField) {
        return addReturnField(new QueryReturnField(returnField));
    }

    /**
     * Add the name of the field to be returned in the result set.
     * @param returnParameter Return parameter.
     * @return Return the same instance of this class.
     */
    public final Query addReturnField(QueryReturnParameter returnParameter) {
        returnParameters.add((QueryReturnParameter) checkQueryParameter((QueryParameter) returnParameter));
        return this;
    }

    /**
     * Add join instance to the query.
     * @param join Join instance.
     */
    public final void addJoin(Join join) {
        if(join != null && !joins.contains(join)) {
            joins.add(join);
        } else {
            if(join == null) {
                throw new NullPointerException("Null join instance");
            }
        }
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
    public final <O extends Object> Set<O> evaluate(Collection<O> dataSource, Object... parameters) {
        return evaluate((query) -> dataSource, new IntrospectionConsumer<>(), parameters);
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
    public final <O extends Object> Set<O> evaluate(Collection<O> dataSource, Consumer<O> consumer, Object... parameters) {
        return evaluate((query) -> dataSource, consumer, parameters);
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
    public final <O extends Object> Set<O> evaluate(DataSource<O> dataSource, Object... parameters) {
        return evaluate(dataSource, new IntrospectionConsumer<>(), parameters);
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
    public final <O extends Object> Set<O> evaluate(DataSource<O> dataSource, Consumer<O> consumer, Object... parameters) {
        Set<O> result;

        //Creating result data collection.
        if(orderParameters.size() > 0) {
            //If the query has order fields then creates a tree set with
            //a comparator using the order fields.
            result = new TreeSet<>((o1, o2) -> {
                int compareResult = 0;

                Comparable<Object> comparable1;
                Comparable<Object> comparable2;
                for (QueryOrderParameter orderField : orderParameters) {
                    try {
                        comparable1 = consumer.get(o1, (QueryParameter) orderField);
                        comparable2 = consumer.get(o2, (QueryParameter) orderField);
                    } catch (ClassCastException ex) {
                        throw new IllegalArgumentException("Order field must be comparable");
                    }

                    if(comparable1 == null ^ comparable2 == null) {
                        compareResult += (comparable1 == null) ? -1 : 1;
                    } else if(comparable1 == null && comparable2 == null) {
                        compareResult += 0;
                    } else {
                        compareResult += comparable1.compareTo(comparable2) * (orderField.isDesc() ? -1 : 1);
                    }

                    if(compareResult != 0) {
                        break;
                    }
                }

                if (compareResult == 0) {
                    compareResult = o1.hashCode() - o2.hashCode();
                }

                return compareResult;
            });
        } else {
            //If the query has not order fields then creates a linked hash set to
            //maintain the natural order of the data.
            result = new LinkedHashSet<>();
        }

        Map<Evaluator,Object> valuesMap = createValuesMap(this, dataSource, consumer, parameters);

        //Getting data from data source.
        Collection<O> data;
        if(joins.size() > 0) {
            //If the query has joins then data source must return the joined data
            //collection using all the resources
            data = (Collection<O>) join((DataSource<Joinable>) dataSource, (Consumer<Joinable>) consumer, valuesMap);
        } else {
            //Creates the first query for the original resource.
            Query resolveQuery = new Query(getResourceName());
            if(getStart() != null) {
                resolveQuery.setLimit(getLimit() + getStart());
            } else {
                resolveQuery.setLimit(getLimit());
            }
            resolveQuery.returnParameters.addAll(this.returnParameters);
            copyEvaluators(resolveQuery, this, valuesMap);

            //If the query has not joins then data source must return data from
            //resource of the query.
            data = dataSource.getResourceData(resolveQuery);
        }

        boolean groupResult = false;
        Map<GroupableIndex, Map<Query.QueryReturnGroupingFunction, List<Object>>> groupingMap = null;
        Map<GroupableIndex, Groupable> parcelMap = null;
        Map<Query.QueryReturnGroupingFunction, List<Object>> functionMap;
        List<Object> valuesByfunction;
        GroupableIndex groupableIndex;
        Object[] indexes;
        if(!groupParameters.isEmpty()) {
            groupingMap = new HashMap<>();
            parcelMap = new HashMap<>();
            groupResult = true;
        }

        //Filtering data
        boolean add;
        int start = getStart() == null ? 0 : getStart();
        if(start < data.size()) {
            for (O object : data) {
                add = true;
                for (Evaluator evaluator : getEvaluators()) {
                    add = evaluator.evaluate(object, consumer, valuesMap);
                    if (!add) {
                        break;
                    }
                }
                if (add) {
                    if(groupResult) {
                        if(!(object instanceof Groupable)) {
                            //Creates an instance of groupable index
                            int i = 0;
                            indexes = new Object[groupParameters.size()];
                            for (QueryField field : groupParameters) {
                                indexes[i++] = consumer.get(object, field);
                            }
                            groupableIndex = new GroupableIndex(indexes);
                            functionMap = groupingMap.get(groupableIndex);
                            if (functionMap == null) {
                                functionMap = new HashMap<>();
                                parcelMap.put(groupableIndex, (Groupable) object);
                                groupingMap.put(groupableIndex, functionMap);
                            }
                            for (QueryReturnParameter returnParameter : getReturnParameters()) {
                                if (returnParameter instanceof QueryReturnGroupingFunction) {
                                    valuesByfunction = functionMap.get(returnParameter);
                                    if (valuesByfunction == null) {
                                        valuesByfunction = new ArrayList<>();
                                        functionMap.put((QueryReturnGroupingFunction) returnParameter, valuesByfunction);
                                    }
                                    valuesByfunction.add(object);
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("");
                        }
                    } else {
                        if(object instanceof Enlarged) {
                            for(QueryReturnParameter returnParameter : getReturnParameters()) {
                                if(returnParameter instanceof QueryReturnField) {
                                    QueryReturnField returnField = (QueryReturnField) returnParameter;
                                    if (returnField.getAlias() != null) {
                                        ((Enlarged) object).put(returnField.getAlias(), ((Enlarged) object).get(returnField.getFieldName()));
                                    }
                                } else if(returnParameter instanceof QueryReturnGroupingFunction) {
                                    //Do nothing because the grouping functions only must be used when the query is grouped
                                } else if(returnParameter instanceof QueryReturnFunction) {
                                    QueryReturnFunction function = (QueryReturnFunction) returnParameter;
                                    ((Enlarged)object).put(function.getAlias(),
                                            consumer.resolveFunction(function, object));
                                }
                            }
                        }

                        result.add(object);
                    }
                }
                if (getLimit() != null && result.size() == (start + getLimit())) {
                    break;
                }
            }

            if (start > 0) {
                result = result.stream().skip(start).collect(Collectors.toSet());
            }

            if(groupResult) {
                for(GroupableIndex index : parcelMap.keySet()) {
                    Groupable groupable = parcelMap.get(index);
                    groupable.clear();
                    int i = 0;
                    for (QueryReturnField field : groupParameters) {
                        groupable.put(field.getAlias(), index.indexes[i++]);
                    }

                    for (QueryReturnGroupingFunction queryReturnGroupingFunction : groupingMap.get(index).keySet()) {
                        groupable.put(queryReturnGroupingFunction.getAlias(),
                                consumer.resolveFunction(queryReturnGroupingFunction,
                                        groupingMap.get(index).get(queryReturnGroupingFunction)));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Copy all the evaluator from the source collection to destiny collection.
     * @param dest Destiny collection.
     * @param src Source collection.
     * @param valuesMap Resolved values.
     */
    private void copyEvaluators(EvaluatorCollection dest, EvaluatorCollection src, Map<Evaluator, Object> valuesMap) {
        for(Evaluator evaluator : src.getEvaluators()) {
            if(evaluator instanceof FieldEvaluator) {
                dest.addEvaluator(((FieldEvaluator)evaluator).copy(valuesMap.get(evaluator)));
            } else if(evaluator instanceof And) {
                copyEvaluators(dest.and(), (EvaluatorCollection) evaluator, valuesMap);
            } else if(evaluator instanceof Or) {
                copyEvaluators(dest.or(), (EvaluatorCollection) evaluator, valuesMap);
            }
        }
    }

    /**
     * Creates a map with the value for all the unresolved values and raw values into
     * each field evaluator.
     * @param collection Evaluator collection.
     * @param dataSource Data source.
     * @param consumer Consumer.
     * @param parameters Parameters.
     * @return Map with all the needed values.
     */
    private Map<Evaluator, Object> createValuesMap(EvaluatorCollection collection,
                                                   DataSource dataSource, Consumer consumer,
                                                   Object... parameters){
        Map<Evaluator, Object> result = new HashMap<>();
        for(Evaluator evaluator : collection.getEvaluators()) {
            if(evaluator instanceof FieldEvaluator) {
                result.put(evaluator, ((FieldEvaluator)evaluator).getValue(dataSource, consumer, parameters));
            } else if(evaluator instanceof EvaluatorCollection) {
                result.putAll(createValuesMap((EvaluatorCollection)evaluator, dataSource, consumer, parameters));
            }
        }
        return result;
    }

    /**
     * Create a joined data from data source using the joins instances stored in the query.
     * @param dataSource Data souce.
     * @param consumer Consumer.
     * @return Joined data collection.
     */
    private Collection<Joinable> join(DataSource<Joinable> dataSource, Consumer<Joinable> consumer, Map<Evaluator,Object> valuesMap) {
        Collection<Joinable> result = new ArrayList<>();

        //Creates the first query for the original resource.
        Query joinQuery = new Query(getResourceName());
        joinQuery.returnParameters.addAll(this.returnParameters);
        for(Evaluator evaluator : getEvaluatorsFromResource(this, joinQuery, getResource())) {
            joinQuery.addEvaluator(((FieldEvaluator)evaluator).copy(valuesMap.get(evaluator)));
        }
        //Set the first query as start by default
        Query startQuery = joinQuery;

        //Put the first query in the list
        List<Query> queries = new ArrayList<>();
        queries.add(joinQuery);

        //Build a query for each join and evaluate the better filter to start
        int queryStart = 0;
        int joinStart = 0;
        for (int i = 0; i < joins.size(); i++) {
            Join join = joins.get(i);
            joinQuery = new Query(join.getResourceName());
            joinQuery.addReturnField("*");
            for (Evaluator evaluator : join.getEvaluators()) {
                joinQuery.addEvaluator(evaluator);
            }
            for (Evaluator evaluator : getEvaluatorsFromResource(this, joinQuery, join.getResource())) {
                joinQuery.addEvaluator(evaluator);
            }
            queries.add(joinQuery);

            if(joinQuery.getEvaluators().size() > startQuery.getEvaluators().size()) {
                startQuery = joinQuery;
                queryStart = i+1;
                joinStart = i;
            }
        }

        Map<Object, Set<Joinable>> indexedJoineables;
        Collection<Joinable> leftJoinables = new ArrayList<>();
        Collection<Joinable> rightJoinables = new ArrayList<>();
        In in;
        Join queryJoin = null;
        Set<Object> keys;
        QueryField firstField;
        QueryField secondField;

        //Evaluate from the start query to right
        int j = joinStart;
        for (int i = queryStart; i < queries.size(); i++) {
            joinQuery = queries.get(i);
            if(leftJoinables.isEmpty()) {
                leftJoinables.addAll(dataSource.getResourceData(joinQuery));
            } else {
                queryJoin = joins.get(j);

                if(queryJoin.getLeftField().getResource().equals(queryJoin.getResource())) {
                    //If the left field of the join has the same resource that join then the
                    //right field index the accumulated data.
                    firstField = queryJoin.getRightField();
                    secondField = queryJoin.getLeftField();
                } else {
                    //If the right field of the join has the same resource that join then the
                    //left field index the accumulated data.
                    firstField = queryJoin.getLeftField();
                    secondField = queryJoin.getRightField();
                }

                indexedJoineables = index(leftJoinables, firstField, consumer);
                leftJoinables.clear();
                keys = indexedJoineables.keySet();
                joinQuery.addEvaluator(new In(secondField.toString(), keys));
                rightJoinables.addAll(dataSource.getResourceData(joinQuery));
                for (Joinable rightJoinable : rightJoinables) {
                    for (Joinable leftJoinable : indexedJoineables.get(consumer.get(rightJoinable, secondField))) {
                        leftJoinables.add(leftJoinable.join(rightJoinable));
                    }
                }
                j++;
            }
        }

        rightJoinables = leftJoinables;
        leftJoinables = new ArrayList<>();

        //Evaluate from the start query to left
        j = joinStart;
        for (int i = queryStart - 1; i >= 0; i--, j--) {
            joinQuery = queries.get(i);
            queryJoin = joins.get(j);

            if(queryJoin.getLeftField().getResource().equals(queryJoin.getResource())) {
                //If the right field of the join has the same resource that join then the
                //left field index the accumulated data.
                firstField = queryJoin.getLeftField();
                secondField = queryJoin.getRightField();
            } else {
                //If the left field of the join has the same resource that join then the
                //right field index the accumulated data.
                firstField = queryJoin.getRightField();
                secondField = queryJoin.getLeftField();
            }

            indexedJoineables = index(rightJoinables, firstField, consumer);
            rightJoinables.clear();
            keys = indexedJoineables.keySet();
            joinQuery.addEvaluator(new In(secondField.toString(), keys));
            leftJoinables.addAll(dataSource.getResourceData(joinQuery));
            for (Joinable leftJoinable : leftJoinables) {
                for (Joinable rightJoinable : indexedJoineables.get(consumer.get(leftJoinable, secondField))) {
                    rightJoinables.add(rightJoinable.join(leftJoinable));
                }
            }
        }

        result.addAll(rightJoinables);
        return result;
    }

    /**
     * Return the list of evaluator for the specific resource.
     * @param collection Evaluator collection.
     * @param resource Resource type.
     * @return List of evaluators.
     */
    private List<Evaluator> getEvaluatorsFromResource(EvaluatorCollection collection, EvaluatorCollection parent, QueryResource resource) {
        List<Evaluator> result = new ArrayList<>();
        for(Evaluator evaluator : collection.getEvaluators()) {
            if(evaluator instanceof FieldEvaluator) {
                QueryParameter queryParameter = ((FieldEvaluator) evaluator).getQueryParameter();
                if((queryParameter instanceof QueryField &&
                        ((QueryField)queryParameter).getResource().equals(resource)) ||
                        (queryParameter instanceof QueryFunction &&
                                ((QueryFunction)queryParameter).getResources().contains(resource))){
                    result.add(evaluator);
                }
            } else if(evaluator instanceof EvaluatorCollection) {
                EvaluatorCollection subCollection = null;
                if(evaluator instanceof And) {
                    subCollection = new And(parent);
                } else if(evaluator instanceof Or) {
                    subCollection = new Or(parent);
                }
                for(Evaluator subEvaluator : getEvaluatorsFromResource((EvaluatorCollection)evaluator, subCollection, resource)) {
                    subCollection.addEvaluator(subEvaluator);
                }
            }
        }
        return result;
    }

    /**
     * This method evaluate all the values of the collection and put each of values
     * into a map indexed by the value of the parameter field.
     * @param objects Collection of data to evaluate.
     * @param fieldIndex Field to index result.
     * @param consumer Implementation to get the value from the collection
     * @return Return the filtered data indexed by value of the parameter field.
     */
    private final Map<Object, Set<Joinable>> index(Collection<Joinable> objects, QueryField fieldIndex, Consumer<Joinable> consumer) {
        Map<Object, Set<Joinable>> result = new HashMap<>();

        Object key;
        Set<Joinable> set;
        for(Joinable joinable : objects) {
            key = consumer.get(joinable, fieldIndex);
            set = result.get(key);
            if(set == null) {
                set = new HashSet<>();
                result.put(key, set);
            }
            set.add(joinable);
        }

        return result;
    }

    /**
     * Return a copy of this query without all the evaluator and order fields of the
     * parameter collections.
     * @param evaluatorsToRemove Evaluators to reduce.
     * @return Reduced copy of the query.
     */
    public final Query reduce(Collection<Evaluator> evaluatorsToRemove) {
        Query copy = new Query(this);
        copy.evaluators.addAll(this.evaluators);

        if(evaluatorsToRemove != null && !evaluatorsToRemove.isEmpty()) {
            reduceCollection(copy, evaluatorsToRemove);
        }

        return copy;
    }

    /**
     * Reduce recursively all the collection into the query.
     * @param collection Collection to reduce.
     * @param evaluatorsToRemove Evaluator to remove.
     */
    private final void reduceCollection(EvaluatorCollection collection, Collection<Evaluator> evaluatorsToRemove) {
        for(Evaluator evaluatorToRemove : evaluatorsToRemove) {
            collection.evaluators.remove(evaluatorToRemove);
            collection.addEvaluator(new TrueEvaluator());
        }

        for(Evaluator evaluator : collection.evaluators) {
            if(evaluator instanceof Or || evaluator instanceof And) {
                reduceCollection((EvaluatorCollection)evaluator, evaluatorsToRemove);
            }
        }
    }

    /**
     * Creates a string representation of the query object.
     * @return String representation.
     */
    @Override
    public String toString() {
        Strings.Builder result = new Strings.Builder();

        //Print select
        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT));
        result.append(Strings.WHITE_SPACE);
        for(QueryReturnParameter field : getReturnParameters()) {
            result.append(field);
            if(field.getAlias() != null) {
                result.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.AS));
                result.append(Strings.WHITE_SPACE).append(field.getAlias());
            }
            result.append(Strings.EMPTY_STRING, SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR));
        }
        result.cleanBuffer();

        //Print from
        result.append(Strings.WHITE_SPACE);
        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.FROM));
        result.append(Strings.WHITE_SPACE);
        result.append(getResourceName());
        result.append(Strings.WHITE_SPACE);

        //Print joins
        for(Join join : joins) {
            if(!(join.getType() == Join.JoinType.JOIN)) {
                result.append(join.getType());
                result.append(Strings.WHITE_SPACE);
            }
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.JOIN)).append(Strings.WHITE_SPACE);
            result.append(join.getResourceName()).append(Strings.WHITE_SPACE);
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.ON)).append(Strings.WHITE_SPACE);
            result.append(join.getLeftField()).append(Strings.WHITE_SPACE);
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS)).append(Strings.WHITE_SPACE);
            result.append(join.getRightField()).append(Strings.WHITE_SPACE);
            if(join.getEvaluators().size() > 0) {
                result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.AND)).append(Strings.WHITE_SPACE);
                toStringEvaluatorCollection(result, join);
            }
        }

        if(evaluators.size() > 0) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.WHERE)).append(Strings.WHITE_SPACE);
            toStringEvaluatorCollection(result, this);
        }

        if(groupParameters.size() > 0) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.GROUP_BY)).append(Strings.WHITE_SPACE);
            for(QueryField groupParameter : groupParameters) {
                result.append(groupParameter, SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR);
            }
            result.cleanBuffer();
        }

        if(orderParameters.size() > 0) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.ORDER_BY)).append(Strings.WHITE_SPACE);
            for(QueryOrderParameter orderField : orderParameters) {
                result.append(orderField);
                if(orderField.isDesc()) {
                    result.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC));
                }
                result.append(Strings.EMPTY_STRING, SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR));
            }
            result.cleanBuffer();
        }

        if(getStart() != null) {
            result.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.START));
            result.append(Strings.WHITE_SPACE).append(getStart());
        }

        if(getLimit() != null) {
            result.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT));
            result.append(Strings.WHITE_SPACE).append(getLimit());
        }

        return result.toString();
    }

    /**
     * Creates a string representation of evaluator collection.
     * @param result Buffer with the current result.
     * @param collection Collection in order to create the string representation.
     */
    private void toStringEvaluatorCollection(Strings.Builder result, EvaluatorCollection collection) {
        String separator = Strings.EMPTY_STRING;
        String separatorValue = collection instanceof Or ?
                SystemProperties.get(SystemProperties.Query.ReservedWord.OR) :
                SystemProperties.get(SystemProperties.Query.ReservedWord.AND);
        for(Evaluator evaluator : collection.getEvaluators()) {
            if(evaluator instanceof Or) {
                result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.OR)).append(Strings.WHITE_SPACE);
                if(((Or)evaluator).getEvaluators().size() == 1) {
                    toStringEvaluatorCollection(result, (Or) evaluator);
                } else {
                    result.append(Strings.START_GROUP);
                    toStringEvaluatorCollection(result, (Or) evaluator);
                    result.append(Strings.END_GROUP);
                }
            } else if(evaluator instanceof And) {
                result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.AND)).append(Strings.WHITE_SPACE);
                if(collection instanceof Query) {
                    toStringEvaluatorCollection(result, (And) evaluator);
                } else {
                    if(((And)evaluator).getEvaluators().size() == 1) {
                        toStringEvaluatorCollection(result, (And) evaluator);
                    } else {
                        result.append(Strings.START_GROUP);
                        toStringEvaluatorCollection(result, (And) evaluator);
                        result.append(Strings.END_GROUP);
                    }
                }
            } else if(evaluator instanceof FieldEvaluator) {
                result.append(separator);
                FieldEvaluator fieldEvaluator = (FieldEvaluator) evaluator;
                result.append(fieldEvaluator.getQueryParameter()).append(Strings.WHITE_SPACE);
                if (fieldEvaluator instanceof Distinct) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof Equals) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof GreaterThanOrEqual) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof GreaterThan) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof NotIn) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_IN)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof In) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.IN)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof Like) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof SmallerThanOrEqual) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN_OR_EQUALS)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof SmallerThan) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN)).append(Strings.WHITE_SPACE);
                }
                if(fieldEvaluator.getRawValue() == null) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL));
                } else {
                    result = toStringFieldEvaluatorValue(fieldEvaluator.getRawValue(), fieldEvaluator.getValueType(), result);
                }
                result.append(Strings.WHITE_SPACE);
            }
            separator = separatorValue + Strings.WHITE_SPACE;
        }
    }

    /**
     * Creates the string representation of the field evaluator.
     * @param value Object to create the string representation.
     * @param type Object type.
     * @param result Buffer with the current result.
     * @return String representation of the field evaluator.
     */
    private static Strings.Builder toStringFieldEvaluatorValue(Object value, Class type, Strings.Builder result) {
        if(FieldEvaluator.ReplaceableValue.class.isAssignableFrom(type)) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.REPLACEABLE_VALUE));
        } else if(FieldEvaluator.QueryValue.class.isAssignableFrom(type)) {
            result.append(Strings.START_GROUP);
            result.append(((FieldEvaluator.QueryValue)value).getQuery().toString());
            result.append(Strings.END_GROUP);
        } else if(String.class.isAssignableFrom(type)) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER));
            result.append(value);
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER));
        } else if(Date.class.isAssignableFrom(type)) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER));
            result.append(SystemProperties.getDateFormat(SystemProperties.Query.DATE_FORMAT).format((Date)value));
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER));
        } else if(Collection.class.isAssignableFrom(type)) {
            result.append(Strings.START_GROUP);
            String separator = Strings.EMPTY_STRING;
            for(Object object : (Collection)value) {
                if(object != null) {
                    result.append(separator);
                    result = toStringFieldEvaluatorValue(object, object.getClass(), result);
                    separator = SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR);
                }
            }
            result.append(Strings.END_GROUP);
        } else {
            result.append(value.toString());
        }
        return result;
    }

    /**
     * Create a query instance from sql definition.
     * @param sql Sql definition.
     * @return Query instance.
     */
    public static Query compile(String sql) {
        List<String> groups = Strings.replaceableGroup(sql);
        return compile(groups, groups.size() -1);
    }

    public static Collection<JoinableMap> evaluate(String query) {
        return compile(query).evaluate(new CrudDataSource());
    }

    public static Collection<JoinableMap> evaluate(Query query) {
        return query.evaluate(new CrudDataSource());
    }

    /**
     * Create a query instance from sql definition.
     * @param groups
     * @param startGroup
     * @return Query instance.
     */
    private static Query compile(List<String> groups, Integer startGroup) {
        Query query;
        Pattern pattern = SystemProperties.getPattern(SystemProperties.Query.SELECT_REGULAR_EXPRESSION);
        Matcher matcher = pattern.matcher(groups.get(startGroup));

        if(matcher.matches()) {
            String selectBody = matcher.group(SystemProperties.getInteger(SystemProperties.Query.SELECT_GROUP_INDEX));
            selectBody = selectBody.replaceFirst(Strings.CASE_INSENSITIVE_REGEX_FLAG+SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT), Strings.EMPTY_STRING);
            String fromBody = matcher.group(SystemProperties.getInteger(SystemProperties.Query.FROM_GROUP_INDEX));
            fromBody = fromBody.replaceFirst(Strings.CASE_INSENSITIVE_REGEX_FLAG+SystemProperties.get(SystemProperties.Query.ReservedWord.FROM), Strings.EMPTY_STRING);
            String conditionalBody = matcher.group(SystemProperties.getInteger(SystemProperties.Query.CONDITIONAL_GROUP_INDEX));
            if(conditionalBody != null && conditionalBody.endsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STATEMENT_END))) {
                conditionalBody = conditionalBody.substring(0, conditionalBody.indexOf(SystemProperties.get(SystemProperties.Query.ReservedWord.STATEMENT_END))-1);
            }

            query = new Query(fromBody.trim());

            for(String returnField : selectBody.split(SystemProperties.get(
                    SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                query.addReturnField((QueryReturnParameter)
                        processStringValue(groups, returnField, null, QueryReturnParameter.class));
            }

            if(conditionalBody != null) {
                Pattern conditionalPatter = SystemProperties.getPattern(SystemProperties.Query.CONDITIONAL_REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
                String[] conditionalElements = conditionalPatter.split(conditionalBody);
                String element;
                String elementValue;
                for (int i = 0; i < conditionalElements.length; i++) {
                    element = conditionalElements[i++].trim();
                    elementValue = conditionalElements[i].trim();
                    if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.JOIN)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.INNER)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LEFT)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.RIGHT))) {

                        Join.JoinType type = Join.JoinType.valueOf(element.toUpperCase());
                        if(type != Join.JoinType.JOIN) {
                            elementValue = conditionalElements[++i].trim();
                        }

                        String[] joinElements = elementValue.split(SystemProperties.get(SystemProperties.Query.JOIN_REGULAR_EXPRESSION));
                        String joinResource = joinElements[SystemProperties.getInteger(SystemProperties.Query.JOIN_RESOURCE_NAME_INDEX)].trim();
                        String joinEvaluators = joinElements[SystemProperties.getInteger(SystemProperties.Query.JOIN_EVALUATORS_INDEX)].trim();
                        if(joinEvaluators.startsWith(Strings.REPLACEABLE_GROUP)) {
                            joinEvaluators = groups.get(Integer.parseInt(joinEvaluators.replace(Strings.REPLACEABLE_GROUP, Strings.EMPTY_STRING)));
                        }

                        Join join = new Join(query, joinResource, type);
                        completeEvaluatorCollection(joinEvaluators, groups, join, 0, new AtomicInteger(0));
                        query.addJoin(join);
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.WHERE))) {
                        completeEvaluatorCollection(elementValue, groups, query, 0, new AtomicInteger(0));
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.ORDER_BY))) {
                        for (String orderField : elementValue.split(SystemProperties.get(
                                SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                            query.addOrderField((QueryOrderParameter)
                                    processStringValue(groups, orderField, null, QueryOrderParameter.class));
                        }
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GROUP_BY))) {
                        for (String orderField : elementValue.split(SystemProperties.get(
                                SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                            query.addGroupField((QueryReturnField)
                                    processStringValue(groups, orderField, null, QueryReturnParameter.class));
                        }
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT))) {
                        query.setLimit(Integer.parseInt(elementValue));
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.START))) {
                        query.setStart(Integer.parseInt(elementValue));
                    }
                }
            }
        } else {
            MatchResult mr = matcher.toMatchResult();
            throw new IllegalArgumentException();
        }

        return query;
    }

    /**
     * Complete the evaluator collections with all the evaluator definitions in the groups.
     * @param groups Where groups.
     * @param parentCollection Parent collection.
     * @param definitionIndex Definition index into the groups.
     */
    private static final void completeEvaluatorCollection(String startElement, List<String> groups,
                                                          EvaluatorCollection parentCollection,
                                                          Integer definitionIndex,
                                                          AtomicInteger placesIndex) {
        Pattern wherePatter = SystemProperties.getPattern(SystemProperties.Query.EVALUATOR_COLLECTION_REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
        String[] evaluatorDefinitions;
        if(startElement != null) {
            evaluatorDefinitions = wherePatter.split(startElement);
        } else {
            evaluatorDefinitions = wherePatter.split(groups.get(definitionIndex));
        }
        EvaluatorCollection collection = null;
        List<String> pendingDefinitions = new ArrayList<>();
        for(String definition : evaluatorDefinitions) {
            definition = definition.trim();
            if (definition.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.AND))) {
                if (collection == null) {
                    if(parentCollection instanceof Query || parentCollection instanceof Join || parentCollection instanceof And) {
                        collection = parentCollection;
                    } else {
                        collection = parentCollection.and();
                    }
                } else if (collection instanceof Or) {
                    if(parentCollection instanceof Query || parentCollection instanceof Join || parentCollection instanceof And) {
                        collection = parentCollection;
                    } else {
                        collection = parentCollection.and();
                    }
                }
            } else if (definition.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.OR))) {
                if (collection == null) {
                    if(parentCollection instanceof Or) {
                        collection = parentCollection;
                    } else {
                        collection = parentCollection.or();
                    }
                } else if(collection instanceof Query || collection instanceof Join || collection instanceof And) {
                    if(parentCollection instanceof Or) {
                        collection = parentCollection;
                    } else {
                        collection = parentCollection.or();
                    }
                }
            } else {
                pendingDefinitions.add(definition);
                if(collection != null) {
                    for(String pendingDefinition : pendingDefinitions) {
                        processDefinition(pendingDefinition, collection, groups, placesIndex);
                    }
                    pendingDefinitions.clear();
                } else if(pendingDefinitions.size() > 1) {
                    throw new IllegalArgumentException("");
                }
            }
        }

        for(String pendingDefinition : pendingDefinitions) {
            if(collection != null) {
                processDefinition(pendingDefinition, collection, groups, placesIndex);
            } else {
                processDefinition(pendingDefinition, parentCollection, groups, placesIndex);
            }
        }
    }

    /**
     * Creates a conditional evaluator from string representation.
     * @param definition String definition of the conditional.
     * @param collection Evaluator collection to put the conditional processed.
     * @param groups Sub representation of the main representation.
     * @param placesIndex Place counter of the group list.
     */
    private static void processDefinition(String definition, EvaluatorCollection collection, List<String> groups, AtomicInteger placesIndex) {
        String[] evaluatorValues;
        Object firstObject;
        Object secondObject;
        String firstArgument;
        String secondArgument;
        String operator;
        Evaluator evaluator = null;
        QueryParameter queryParameter;
        Object value;

        if (definition.startsWith(Strings.REPLACEABLE_GROUP)) {
            Integer index = Integer.parseInt(definition.replace(Strings.REPLACEABLE_GROUP, Strings.EMPTY_STRING));
            completeEvaluatorCollection(null, groups, collection, index, placesIndex);
        } else {
            evaluatorValues = definition.split(SystemProperties.get(SystemProperties.Query.OPERATION_REGULAR_EXPRESSION));
            if (evaluatorValues.length >= 3) {

                boolean operatorDone = false;
                firstArgument = Strings.EMPTY_STRING;
                secondArgument = Strings.EMPTY_STRING;
                operator = Strings.EMPTY_STRING;
                for (String evaluatorValue : evaluatorValues) {
                    evaluatorValue = evaluatorValue.trim();
                    if (evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT))) {
                        operator += evaluatorValue + Strings.WHITE_SPACE;
                        operatorDone = true;
                    } else if (evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT))
                            || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT_2))
                            || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS))
                            || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN))
                            || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS))
                            || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.IN))
                            || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE))
                            || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN))
                            || evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN_OR_EQUALS))) {
                        operator += evaluatorValue;
                        operatorDone = true;
                    } else if (operatorDone) {
                        secondArgument += evaluatorValue + Strings.WHITE_SPACE;
                    } else {
                        firstArgument += evaluatorValue + Strings.WHITE_SPACE;
                    }
                }

                if (operator == null) {
                    throw new IllegalArgumentException("Operator not found for expression: " + definition);
                }

                firstObject = processStringValue(groups, firstArgument.trim(), placesIndex, QueryParameter.class);
                secondObject = processStringValue(groups, secondArgument.trim(), placesIndex, QueryParameter.class);
                operator = operator.trim();

                if(firstObject instanceof QueryParameter) {
                    queryParameter = (QueryParameter) firstObject;
                    value = secondObject;
                } else if(secondObject instanceof QueryParameter) {
                    queryParameter = (QueryParameter) secondObject;
                    value = firstObject;
                } else {
                    throw new IllegalArgumentException("");
                }

                if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT))) {
                    evaluator = new Distinct(queryParameter, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT_2))) {
                    evaluator = new Distinct(queryParameter, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS))) {
                    evaluator = new Equals(queryParameter, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN))) {
                    evaluator = new GreaterThan(queryParameter, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS))) {
                    evaluator = new GreaterThanOrEqual(queryParameter, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.IN))) {
                    evaluator = new In(queryParameter, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE))) {
                    evaluator = new Like(queryParameter, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_IN))) {
                    evaluator = new NotIn(queryParameter, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN))) {
                    evaluator = new SmallerThan(queryParameter, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN_OR_EQUALS))) {
                    evaluator = new SmallerThanOrEqual(queryParameter, value);
                }

                collection.addEvaluator(evaluator);
            } else {
                throw new IllegalArgumentException("Syntax error for expression: " + definition + ", expected {field} {operator} {value}");
            }
        }
    }

    /**
     * Process the string representation to obtain the specific object type.
     * @param groups Sub representation of the main representation.
     * @param stringValue String representation to process.
     * @param placesIndex Place counter of the group list.
     * @param parameterClass Parameter class.
     * @return Return the specific implementation of the string representation.
     */
    private static Object processStringValue(List<String> groups, String stringValue, AtomicInteger placesIndex, Class parameterClass) {
        Object result = null;
        if(stringValue.equals(SystemProperties.get(SystemProperties.Query.ReservedWord.REPLACEABLE_VALUE))) {
            //If the string value is equals than "?" then the value object is an instance of ReplaceableValue.
            result = new FieldEvaluator.ReplaceableValue(placesIndex.getAndAdd(1));
        } else if(stringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL))) {
            result = null;
        } else if(stringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.TRUE))) {
            result = true;
        } else if(stringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.FALSE))) {
            result = false;
        } else if(stringValue.startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER))) {
            if (stringValue.endsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER))) {
                //If the string value start and end with "'" then the value can be a string or a date object.
                stringValue = stringValue.substring(1, stringValue.length() - 1);
                try {
                    result = SystemProperties.getDateFormat(SystemProperties.Query.DATE_FORMAT).parse(stringValue);
                } catch (Exception ex) {
                    //The value is not a date
                    result = stringValue;
                }
            } else {
                throw new IllegalArgumentException("");
            }
        } else if(stringValue.startsWith(Strings.REPLACEABLE_GROUP)) {
            Integer index = Integer.parseInt(stringValue.replace(Strings.REPLACEABLE_GROUP, Strings.EMPTY_STRING));
            String group = groups.get(index);
            if(group.toUpperCase().startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT))) {
                result = new FieldEvaluator.QueryValue(Query.compile(groups, index));
            } else {
                //If the string value start with "(" and end with ")" then the value is a collection.
                Collection<Object> collection = new ArrayList<>();
                for (String subStringValue : group.split(SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                    collection.add(processStringValue(groups, subStringValue.trim(), placesIndex, parameterClass));
                }
                result = collection;
            }
        } else if(stringValue.matches(SystemProperties.get(SystemProperties.HCJF_UUID_REGEX))) {
            result = UUID.fromString(stringValue);
        } else if(stringValue.matches(SystemProperties.get(SystemProperties.HCJF_INTEGER_NUMBER_REGEX))) {
            result = Long.parseLong(stringValue);
        } else if(stringValue.matches(SystemProperties.get(SystemProperties.HCJF_DECIMAL_NUMBER_REGEX))) {
            try {
                result = SystemProperties.getDecimalFormat(SystemProperties.Query.DECIMAL_FORMAT).parse(stringValue);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Unable to parse decimal number");
            }
        } else if(stringValue.matches(SystemProperties.get(SystemProperties.HCJF_SCIENTIFIC_NUMBER_REGEX))) {
            try {
                result = SystemProperties.getDecimalFormat(SystemProperties.Query.SCIENTIFIC_NOTATION_FORMAT).parse(stringValue);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Unable to parse scientific number");
            }
        } else {
            //Default case, only must be a query parameter.
            String functionName = null;
            String originalValue = null;
            String replaceValue = null;
            String group = null;
            List<Object> functionParameters = null;
            Boolean function = false;
            if(stringValue.contains(Strings.REPLACEABLE_GROUP)) {
                replaceValue = Strings.getGroupIndex(stringValue);
                group = groups.get(Integer.parseInt(replaceValue.replace(Strings.REPLACEABLE_GROUP,Strings.EMPTY_STRING)));
                functionName = stringValue.substring(0, stringValue.indexOf(Strings.REPLACEABLE_GROUP));
                originalValue = stringValue.replace(replaceValue, Strings.START_GROUP + group + Strings.END_GROUP);
                functionParameters = new ArrayList<>();
                for(String param : group.split(SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                    functionParameters.add(processStringValue(groups, param, placesIndex, parameterClass));
                }
                function = true;
            } else {
                originalValue = stringValue;
            }

            if(parameterClass.equals(QueryParameter.class)) {
                if(function) {
                    result = new QueryFunction(originalValue, functionName, functionParameters);
                } else {
                    result = new QueryField(stringValue);
                }
            } else if(parameterClass.equals(QueryReturnParameter.class)) {
                String alias = null;
                String[] parts = originalValue.split(SystemProperties.get(SystemProperties.Query.AS_REGULAR_EXPRESSION));
                if(parts.length == 3) {
                    originalValue = parts[0].trim();
                    alias = parts[2].trim();
                }

                if(function) {
                    result = new QueryReturnFunction(originalValue, functionName, functionParameters, alias);
                } else {
                    result = new QueryReturnField(originalValue, alias);
                }
            } else if(parameterClass.equals(QueryOrderParameter.class)) {
                boolean desc = false;
                if(originalValue.contains(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC))) {
                    originalValue = originalValue.substring(0, originalValue.indexOf(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC))).trim();
                    desc = true;
                }

                if(function) {
                    result = new QueryOrderFunction(originalValue, functionName, functionParameters, desc) ;
                } else {
                    result = new QueryOrderField(originalValue, desc);
                }
            }
        }

        return result;
    }

    /**
     * Represents an query id. Wrapper of the UUID class.
     */
    public static final class QueryId {

        private final UUID id;

        private QueryId() {
            this.id = UUID.randomUUID();
        }

        public QueryId(UUID id) {
            this.id = id;
        }

        /**
         * Get the UUID instance.
         * @return UUID instance.
         */
        public UUID getId() {
            return id;
        }
    }

    /**
     * This class provides an interface to consume a
     * different collection of naming data to be useful in evaluation
     * process.
     */
    public interface Consumer<O extends Object> {

        /**
         * Get naming information from an instance.
         * @param instance Data source.
         * @param queryParameter Query parameter.
         * @return Return the data storage in the data source indexed
         * by the parameter name.
         */
        public <R extends Object> R get(O instance, QueryParameter queryParameter);

        /**
         * This method must resolve the functions that are used into the query object.
         * @param function Query function.
         * @param parameters Parameters to resolve the finction.
         * @param <R> Expected result.
         * @return Return the value obtained of the function resolution.
         */
        public <R extends Object> R resolveFunction(QueryFunction function, Object... parameters);

    }

    public static abstract class DefaultConsumer <O extends Object> implements Consumer<O> {

        /**
         * This method must resolve the functions that are used into the query object.
         * @param function Query function.
         * @param parameters Parameters to resolve the finction.
         * @param <R> Expected result.
         * @return Return the value obtained of the function resolution.
         */
        public <R extends Object> R resolveFunction(QueryFunction function, Object... parameters) {
            return null;
        }

    }

    /**
     * This private class is the default consume method of the queries.
     */
    private static class IntrospectionConsumer<O extends Object> extends DefaultConsumer<O> {

        /**
         * Get naming information from an instance.
         *
         * @param instance    Data source.
         * @param queryParameter Query parameter.
         * @return Return the data storage in the data source indexed
         * by the parameter name.
         */
        @Override
        public <R extends Object> R get(O instance, QueryParameter queryParameter) {
            Object result = null;
            if(queryParameter instanceof QueryField) {
                String fieldName = ((QueryField)queryParameter).getFieldName();
                try {
                    if (instance instanceof JoinableMap) {
                        result = ((JoinableMap) instance).get(fieldName);
                    } else {
                        Introspection.Getter getter = Introspection.getGetters(instance.getClass()).get(fieldName);
                        if (getter != null) {
                            result = getter.get(instance);
                        } else {
                            Log.w(SystemProperties.get(SystemProperties.Query.LOG_TAG),
                                    "Order field not found: %s", fieldName);
                        }
                    }
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Unable to obtain order field value", ex);
                }
            } else if(queryParameter instanceof QueryFunction) {
                throw new UnsupportedOperationException("Function: " + ((QueryFunction)queryParameter).getFunctionName());
            }
            return (R) result;
        }

    }

    /**
     * This interface must implements a provider to obtain the data collection
     * for diferents resources.
     */
    public interface DataSource<O extends Object> {

        /**
         * This method musr return the data of diferents resources using some query.
         * @param query Query object.
         * @return Data collection from the resource.
         */
        public Collection<O> getResourceData(Query query);

    }

    /**
     *
     */
    public static class CrudDataSource implements DataSource<JoinableMap> {

        /**
         *
         * @param query Query object.
         * @return
         */
        @Override
        public Collection<JoinableMap> getResourceData(Query query) {
            return Layers.get(CrudLayerInterface.class, query.getResourceName()).readRows(query);
        }

    }

    private static class GroupableIndex {

        private final Object[] indexes;

        public GroupableIndex(Object[] indexes) {
            this.indexes = indexes;
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = false;

            if(obj instanceof GroupableIndex) {
                GroupableIndex groupableIndex = (GroupableIndex) obj;
                if(groupableIndex.indexes.length == indexes.length) {
                    result = true;
                    for (int i = 0; i < indexes.length; i++) {
                        result &= groupableIndex.indexes[i].equals(indexes[i]);
                        if(!result) {
                            break;
                        }
                    }
                }
            }

            return result;
        }
    }

    /**
     * Group all the query components.
     */
    public interface QueryComponent {}

    /**
     * Represents any kind of resource.
     */
    public static class QueryResource implements Comparable<QueryResource>, QueryComponent {

        private final String resourceName;

        public QueryResource(String resourceName) {
            this.resourceName = resourceName;
        }

        /**
         * Return the resource name.
         * @return Resource name.
         */
        public String getResourceName() {
            return resourceName;
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = false;
            if(obj instanceof QueryResource) {
                result = resourceName.equals(((QueryResource)obj).getResourceName());
            }
            return result;
        }

        @Override
        public int compareTo(QueryResource o) {
            return resourceName.compareTo(o.getResourceName());
        }

        @Override
        public String toString() {
            return getResourceName();
        }
    }

    public static abstract class QueryParameter implements Comparable<QueryParameter>, QueryComponent {

        private final String originalValue;

        public QueryParameter(String originalValue) {
            this.originalValue = originalValue.trim();
        }

        /**
         * Return the original representation of the field.
         * @return Original representation.
         */
        @Override
        public String toString() {
            return originalValue;
        }

        /**
         * Compare the original value of the fields.
         * @param obj Other field.
         * @return True if the fields are equals.
         */
        @Override
        public boolean equals(Object obj) {
            return toString().equals(obj.toString());
        }

        /**
         * Compare the string representation of both objects.
         * @param o Other object.
         * @return Magnitude of the difference between both objects.
         */
        @Override
        public int compareTo(QueryParameter o) {
            return toString().compareTo(o.toString());
        }

    }

    /**
     *
     */
    public static class QueryFunction extends QueryParameter {

        private final String functionName;
        private final List<Object> parameters;

        public QueryFunction(String originalFunction, String functionName, List<Object> parameters) {
            super(originalFunction);
            this.functionName = functionName;
            this.parameters = parameters;
        }

        public String getFunctionName() {
            return functionName;
        }

        public List<Object> getParameters() {
            return parameters;
        }

        public Set<QueryResource> getResources() {
            Set<QueryResource> queryResources = new TreeSet<>();

            for(Object parameter : parameters) {
                if(parameter instanceof QueryField) {
                    queryResources.add(((QueryField)parameter).getResource());
                } else if(parameter instanceof QueryFunction) {
                    queryResources.addAll(((QueryFunction)parameter).getResources());
                }
            }

            return queryResources;
        }
    }

    /**
     * This class represents any kind of query fields.
     */
    public static class QueryField extends QueryParameter {

        private QueryResource resource;
        private String fieldName;
        private final String completeFieldName;
        private final String index;

        public QueryField(String field) {
            super(field);
            if(field.contains(Strings.CLASS_SEPARATOR)) {
                resource = new QueryResource(field.substring(0, field.lastIndexOf(Strings.CLASS_SEPARATOR)));
                this.fieldName = field.substring(field.lastIndexOf(Strings.CLASS_SEPARATOR) + 1).trim();
            } else {
                resource = null;
                this.fieldName = field.trim();
            }

            if(fieldName.contains(Strings.START_SUB_GROUP)) {
                fieldName = fieldName.substring(0, field.indexOf(Strings.START_SUB_GROUP)).trim();
                index = fieldName.substring(field.indexOf(Strings.START_SUB_GROUP) + 1, field.indexOf(Strings.END_SUB_GROUP)).trim();
            } else {
                index = null;
            }

            completeFieldName = (resource == null ? "" : resource + Strings.CLASS_SEPARATOR) + fieldName;
        }

        /**
         * Return the resource of the field.
         * @param resource Field resource.
         */
        protected void setResource(QueryResource resource) {
            this.resource = resource;
        }

        /**
         * Return the resource associated to the field.
         * @return Resource name, can be null.
         */
        public QueryResource getResource() {
            return resource;
        }

        /**
         * Return the field name without associated resource or index.
         * @return Field name.
         */
        public String getFieldName() {
            return fieldName;
        }

        /**
         * Return the resource name and the field name into the same value.
         * @return Complete name.
         */
        public String getCompleteFieldName() {
            return completeFieldName;
        }

        /**
         * Return the index associated to the field.
         * @return Index, can be null.
         */
        public String getIndex() {
            return index;
        }

    }

    public interface QueryReturnParameter extends QueryComponent {

        /**
         * Return the field alias, can be null.
         * @return Field alias.
         */
        public String getAlias();

    }

    /**
     * This kind of component represent the fields to be returned into the query.
     */
    public static class QueryReturnField extends QueryField implements QueryReturnParameter {

        private final String alias;

        public QueryReturnField(String field) {
            this(field, null);
        }

        public QueryReturnField(String field, String alias) {
            super(field);
            this.alias = alias;
        }

        /**
         * Return the field alias, can be null.
         * @return Field alias.
         */
        public String getAlias() {
            return alias;
        }

    }

    public static class QueryReturnFunction extends QueryFunction implements QueryReturnParameter {

        private final String alias;

        public QueryReturnFunction(String originalFunction, String functionName, List<Object> parameters, String alias) {
            super(originalFunction, functionName, parameters);
            this.alias = alias;
        }

        /**
         * Return the field alias, can be null.
         * @return Field alias.
         */
        public String getAlias() {
            return alias;
        }
    }

    public static class QueryReturnGroupingFunction extends QueryReturnFunction {

        public QueryReturnGroupingFunction(String originalFunction, String functionName, List<Object> parameters, String alias) {
            super(originalFunction, functionName, parameters, alias);
        }

    }

    public interface QueryOrderParameter extends QueryComponent {

        /**
         * Return the desc property.
         * @return Desc property.
         */
        public boolean isDesc();

    }

    /**
     * This class represents a order field with desc property
     */
    public static class QueryOrderField extends QueryField implements QueryOrderParameter {

        private final boolean desc;

        public QueryOrderField(String field, boolean desc) {
            super(field);
            this.desc = desc;
        }

        /**
         * Return the desc property.
         * @return Desc property.
         */
        public boolean isDesc() {
            return desc;
        }
    }

    public static class QueryOrderFunction extends QueryFunction implements QueryOrderParameter {

        private final boolean desc;

        public QueryOrderFunction(String originalFunction, String functionName, List<Object> parameters, boolean desc) {
            super(originalFunction, functionName, parameters);
            this.desc = desc;
        }

        /**
         * Return the desc property.
         * @return Desc property.
         */
        public boolean isDesc() {
            return desc;
        }

    }

}
