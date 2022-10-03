package org.hcjf.layers.query;

import org.hcjf.bson.BsonDocument;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.IdentifiableLayerInterface;
import org.hcjf.layers.query.compilers.JsonCompiler;
import org.hcjf.layers.query.compilers.QueryCompiler;
import org.hcjf.layers.query.compilers.SQLCompiler;
import org.hcjf.layers.query.evaluators.*;
import org.hcjf.layers.query.functions.*;
import org.hcjf.layers.query.model.*;
import org.hcjf.layers.query.serializer.QuerySerializer;
import org.hcjf.layers.query.serializer.SQLSerializer;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.service.ServiceThread;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.LruMap;
import org.hcjf.utils.NamedUuid;
import org.hcjf.utils.bson.BsonParcelable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains all the parameter needed to create a query.
 * This kind of queries works over any data collection.
 * @author javaito
 */
public class Query extends EvaluatorCollection implements Queryable {

    public static final String QUERY_BSON_FIELD_NAME = "__query__";
    public static final String DISJOINT_RESULT_SET = "disjointResultSet";
    private static final LruMap<String,Query> cache;

    private final QueryId id;
    private final QueryResource resource;
    private final List<QueryResource> resources;
    private Integer limit;
    private Integer underlyingLimit;
    private Integer start;
    private Integer underlyingStart;
    private final List<QueryReturnParameter> groupParameters;
    private final List<QueryOrderParameter> orderParameters;
    private final List<QueryReturnParameter> returnParameters;
    private final List<Join> joins;
    private final List<Queryable> unions;
    private boolean returnAll;
    private boolean disjoint;
    private Map<String,Object> environment;
    private Map<String,List<QueryReturnFunction>> underlyingFunctions;

    static {
        //Init query compiler cache
        cache = new LruMap<>(SystemProperties.getInteger(SystemProperties.Query.COMPILER_CACHE_SIZE));

        //Publishing compilers
        Layers.publishLayer(SQLCompiler.class);
        Layers.publishLayer(JsonCompiler.class);

        //Publishing serializers
        Layers.publishLayer(SQLSerializer.class);

        //Publishing default function layers...
        Layers.publishLayer(MathQueryFunctionLayer.class);
        Layers.publishLayer(StringQueryFunctionLayer.class);
        Layers.publishLayer(DateQueryFunctionLayer.class);
        Layers.publishLayer(ReferenceFunctionLayer.class);
        Layers.publishLayer(BsonQueryFunctionLayer.class);
        Layers.publishLayer(CollectionQueryFunction.class);
        Layers.publishLayer(ObjectQueryFunction.class);
        Layers.publishLayer(QueryBsonBuilderLayer.class);
        Layers.publishLayer(GeoQueryFunctionLayer.class);
        Layers.publishLayer(ShellQueryFunction.class);

        //Publishing default aggregate function layers...
        Layers.publishLayer(CountQueryAggregateFunctionLayer.class);
        Layers.publishLayer(GetIndexQueryFunctionLayer.class);
        Layers.publishLayer(SumAggregateFunctionLayer.class);
        Layers.publishLayer(ProductAggregateFunctionLayer.class);
        Layers.publishLayer(MeanAggregateFunctionLayer.class);
        Layers.publishLayer(MaxAggregateFunctionLayer.class);
        Layers.publishLayer(MinAggregateFunctionLayer.class);
        Layers.publishLayer(DistinctQueryAggregateFunction.class);
        Layers.publishLayer(GeoUnionAggregateFunctionLayer.class);
        Layers.publishLayer(GeoDistanceAggregateFunctionLayer.class);
        Layers.publishLayer(EvalExpressionAggregateFunctionLayer.class);
        Layers.publishLayer(ContextAggregateFunction.class);
        Layers.publishLayer(PutAggregateFunction.class);
        Layers.publishLayer(AddAggregateFunction.class);
        Layers.publishLayer(ForecastFunctionLayer.class);
    }

    public Query(QueryResource resource, QueryId id) {
        this.id = id;
        this.groupParameters = new ArrayList<>();
        this.orderParameters = new ArrayList<>();
        this.returnParameters = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.resource = resource;
        this.resources = new ArrayList<>();
        this.resources.add(this.resource);
        this.unions = new ArrayList<>();
    }

    public Query(String resource) {
        this(new QueryResource(resource));
    }

    public Query(QueryResource resource){
        this(resource, new QueryId());
    }

    private Query(Query source) {
        super(source);
        this.id = new QueryId();
        this.resource = source.resource;
        this.resources = new ArrayList<>();
        this.resources.add(this.resource);
        this.limit = source.limit;
        this.start = source.start;
        this.returnAll = source.returnAll;
        this.orderParameters = new ArrayList<>();
        this.orderParameters.addAll(source.orderParameters);
        this.returnParameters = new ArrayList<>();
        this.returnParameters.addAll(source.returnParameters);
        this.groupParameters = new ArrayList<>();
        this.groupParameters.addAll(source.groupParameters);
        this.joins = new ArrayList<>();
        this.joins.addAll(source.joins);
        this.unions = new ArrayList<>();
        this.unions.addAll(source.unions);
    }

    /**
     * Returns the query associated to the instance.
     * @return Query instance.
     */
    @Override
    public Query getQuery() {
        return this;
    }

    private QueryParameter checkQueryParameter(QueryParameter queryParameter) {
        if(queryParameter instanceof QueryField) {
            QueryField queryField = (QueryField) queryParameter;
            QueryResource resource = queryField.getResource();
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
            FieldEvaluator fieldEvaluator = (FieldEvaluator) evaluator;
            if(fieldEvaluator.getLeftValue() instanceof QueryParameter) {
                checkQueryParameter((QueryParameter) fieldEvaluator.getLeftValue());
            }
            if(fieldEvaluator.getRightValue() instanceof QueryParameter) {
                checkQueryParameter((QueryParameter) fieldEvaluator.getRightValue());
            }
        }
        return evaluator;
    }

    /**
     * Returns the environment object.
     * @return Environment object.
     */
    public Map<String, Object> getEnvironment() {
        return environment;
    }

    /**
     * Set the environment object.
     * @param environment Environment object.
     */
    public void setEnvironment(Map<String, Object> environment) {
        this.environment = environment;
    }

    /**
     * Returns a list of underlying functions for the current resource name.
     * @return List of underlying functions.
     */
    public List<QueryReturnFunction> getCurrentUnderlyingFunctions() {
        List<QueryReturnFunction> result = null;
        if (getUnderlyingFunctions() != null) {
            result = getUnderlyingFunctions().get(getResourceName());
        }
        return result;
    }

    /**
     * Returns the map with underlying functions.
     * @return Underlying functions.
     */
    public Map<String, List<QueryReturnFunction>> getUnderlyingFunctions() {
        return underlyingFunctions;
    }

    /**
     * Returns the map with underlying functions only for a one particular resource.
     * @param resourceName Resource name.
     * @return Underlying functions.
     */
    private Map<String, List<QueryReturnFunction>> getUnderlyingFunctions(String resourceName) {
        return getUnderlyingFunctionsAndChangeName(resourceName, resourceName);
    }

    /**
     * Returns the map with underlying functions only for a one particular resource and rename the resource into map.
     * @param originalResourceName Resource name.
     * @param newResourceName New name to index the functions into map.
     * @return Underlying functions.
     */
    private Map<String, List<QueryReturnFunction>> getUnderlyingFunctionsAndChangeName(String originalResourceName, String newResourceName) {
        Map<String,List<QueryReturnFunction>> result = new HashMap<>();
        if(underlyingFunctions != null && underlyingFunctions.containsKey(originalResourceName)) {
            result.put(newResourceName, underlyingFunctions.get(originalResourceName));
        }
        return result;
    }

    /**
     * Set the map with underlying functions.
     * @param underlyingFunctions Underlying functions.
     */
    public void setUnderlyingFunctions(Map<String, List<QueryReturnFunction>> underlyingFunctions) {
        this.underlyingFunctions = underlyingFunctions;
    }

    /**
     * Verify if the query indicates return all the fields of the result set.
     * @return Return all.
     */
    public final boolean returnAll() {
        return returnAll || returnParameters.isEmpty();
    }

    /**
     * Returns the parameterized query based in this instance of query.
     * @return Parameterized query instance.
     */
    public final ParameterizedQuery getParameterizedQuery() {
        return new ParameterizedQuery(this);
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
     * Returns the list of unions.
     * @return Unions.
     */
    public List<Queryable> getUnions() {
        return Collections.unmodifiableList(unions);
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
     * Returns the list of resource of the query.
     * @return List of resource fo the query.
     */
    public List<QueryResource> getResources() {
        return resources;
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
     * Returns the query underlying limit.
     * @return Underlying limit.
     */
    public Integer getUnderlyingLimit() {
        return underlyingLimit;
    }

    /**
     * Set the underlying limit.
     * @param underlyingLimit Underlying limit value.
     */
    public void setUnderlyingLimit(Integer underlyingLimit) {
        this.underlyingLimit = underlyingLimit;
    }

    /**
     * Return the object that represents the first element of the result.
     * @return Firts object of the result.
     */
    public final Integer getStart() {
        return start != null ? start : 0;
    }

    /**
     * Set the first object of the result.
     * @param start First object of the result.
     */
    public final void setStart(Integer start) {
        this.start = start;
    }

    /**
     * Returns the underlying start.
     * @return Underlying start value.
     */
    public Integer getUnderlyingStart() {
        return underlyingStart;
    }

    /**
     * Set the underlying start.
     * @param underlyingStart Underlying start value.
     */
    public void setUnderlyingStart(Integer underlyingStart) {
        this.underlyingStart = underlyingStart;
    }

    /**
     * Return all the group fields of the query.
     * @return Group field of the query.
     */
    public List<QueryReturnParameter> getGroupParameters() {
        return Collections.unmodifiableList(groupParameters);
    }

    /**
     * Add a name of the field for group the data collection. This name must be existed
     * like a setter/getter method in the instances of the data collection.
     * @param groupField Name of the pair getter/setter.
     * @return Return the same instance of this class.
     */
    public final Query addGroupField(String groupField) {
        return addGroupField(new QueryReturnField(this, groupField));
    }

    /**
     * Add a name of the field for group the data collection. This name must be existed
     * like a setter/getter method in the instances of the data collection.
     * @param groupField Name of the pair getter/setter.
     * @return Return the same instance of this class.
     */
    public final Query addGroupField(QueryReturnParameter groupField) {
        groupParameters.add((QueryReturnParameter)checkQueryParameter((QueryParameter) groupField));
        return this;
    }

    public boolean isDisjoint() {
        return disjoint;
    }

    public void setDisjoint(boolean disjoint) {
        this.disjoint = disjoint;
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
        return addOrderParameter(new QueryOrderField(this, orderField, desc));
    }

    /**
     * Add a name of the field for order the data collection. This name must be existed
     * like a setter/getter method in the instances of the data collection.
     * @param orderParameter Order parameter.
     * @return Return the same instance of this class.
     */
    public final Query addOrderParameter(QueryOrderParameter orderParameter) {
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
     * Add the name of the field to be returned to the result set.
     * @param returnField Field name.
     * @return Return the same instance of this class.
     */
    public final Query addReturnField(String returnField) {
        if(returnField.equals(SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL))) {
            returnAll = true;
        } else {
            addReturnField(new QueryReturnField(this, returnField));
        }
        return this;
    }

    /**
     * Add the name of the field to be returned to the result set.
     * @param returnParameter Return parameter.
     * @return Return the same instance of this class.
     */
    public final Query addReturnField(QueryReturnParameter returnParameter) {
        if(returnParameter instanceof QueryReturnField && ((QueryReturnField)returnParameter).getFieldPath().equals(
                SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL))) {
            returnAll = true;
        } else {
            returnParameters.add((QueryReturnParameter) checkQueryParameter((QueryParameter) returnParameter));
        }
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
     * Add a union instance to the query.
     * @param queryable Union instance.
     */
    public final void addUnion(Queryable queryable) {
        if(queryable != null) {
            unions.add(queryable);
        } else {
            throw new HCJFRuntimeException("Null union value");
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
    @Override
    public final <O extends Object> Collection<O> evaluate(Collection<O> dataSource) {
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
    public final <O extends Object> Collection<O> evaluate(Collection<O> dataSource, Queryable.Consumer<O> consumer) {
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
    public final <O extends Object> Collection<O> evaluate(Queryable.DataSource<O> dataSource) {
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
    public final <O extends Object> Collection<O> evaluate(Queryable.DataSource<O> dataSource, Queryable.Consumer<O> consumer) {
        Collection<O> result;
        Map<String, Groupable> groupables = null;
        Map<String, Map<String,Object>> disjointResultSets = null;
        List<QueryReturnFunction> aggregateFunctions = new ArrayList<>();
        if(!(Thread.currentThread() instanceof ServiceThread)) {
            //If the current thread is not a service thread then we call this
            //method again using a service thread.
            result = Service.call(()->evaluate(dataSource, consumer), ServiceSession.getGuestSession());
        } else {
            Long totalTime = System.currentTimeMillis();

            //Initialize the evaluators cache because the evaluators in the simple
            //query are valid into the platform evaluation environment.
            initializeEvaluatorsCache();

            //Creating result data collection.
            if (orderParameters.size() > 0) {
                //If the query has order fields then creates a tree set with
                //a comparator using the order fields.
                result = new TreeSet<>((o1, o2) -> {
                    int compareResult = 0;

                    Comparable<Object> comparable1;
                    Comparable<Object> comparable2;
                    for (QueryOrderParameter orderField : orderParameters) {
                        try {
                            if (orderField instanceof QueryOrderFunction) {
                                comparable1 = consumer.resolveFunction(((QueryOrderFunction) orderField), o1, dataSource);
                                comparable2 = consumer.resolveFunction(((QueryOrderFunction) orderField), o2, dataSource);
                            } else {
                                comparable1 = consumer.get(o1, (QueryParameter) orderField, dataSource);
                                comparable2 = consumer.get(o2, (QueryParameter) orderField, dataSource);
                            }
                        } catch (ClassCastException ex) {
                            throw new HCJFRuntimeException("Order field must be comparable");
                        }

                        if (comparable1 == null ^ comparable2 == null) {
                            compareResult = (comparable1 == null) ? -1 : 1;
                        } else if (comparable1 == null && comparable2 == null) {
                            compareResult = 0;
                        } else {
                            compareResult = comparable1.compareTo(comparable2) * (orderField.isDesc() ? -1 : 1);
                        }

                        if (compareResult != 0) {
                            break;
                        }
                    }

                    if (compareResult == 0) {
                        compareResult = o1.hashCode() - o2.hashCode();
                    }

                    return compareResult;
                });
            } else {
                //If the query has not ordered fields then creates a linked hash set to
                //maintain the natural order of the data.
                result = new ArrayList<>();
            }

            Long timeCollectingData = System.currentTimeMillis();
            Integer evaluatingCount = 0;
            Integer formattingCount = 0;
            Long timeEvaluatingConditions = 0L;
            Long timeFormattingData = 0L;
            Set<String> presentFields = new TreeSet<>();

            //Getting data from data source.
            Collection<O> data;
            try {
                if (joins.size() > 0) {
                    data = (Collection<O>) join((DataSource<Joinable>) dataSource, (Consumer<Joinable>) consumer);
                } else {
                    //If the query has not joins then data source must return data from
                    //resource of the query.
                    if(getResource() instanceof QueryDynamicResource) {
                        Query dynamicResourceQuery =  ((QueryDynamicResource)getResource()).getQuery();
                        dynamicResourceQuery.setUnderlyingFunctions(getUnderlyingFunctionsAndChangeName(getResourceName(), dynamicResourceQuery.getResourceName()));
                        data = (Collection<O>) resolveDynamicResource((QueryDynamicResource) getResource(),
                                (DataSource<Joinable>) dataSource, (Consumer<Joinable>) consumer);
                    } else if(getResource() instanceof QueryJsonResource) {
                        data = (Collection<O>) ((QueryJsonResource)getResource()).getResourceValues();
                    } else {
                        //Creates the first query for the original resource.
                        Query resolveQuery = new Query(getResource());
                        resolveQuery.setEnvironment(getEnvironment());
                        resolveQuery.returnAll = true;

                        resolveQuery.setLimit(getLimit());
                        resolveQuery.setUnderlyingLimit(getUnderlyingLimit());
                        resolveQuery.setStart(getStart());
                        resolveQuery.setUnderlyingStart(getUnderlyingStart());
                        resolveQuery.setUnderlyingFunctions(getUnderlyingFunctions(getResourceName()));
                        for(QueryOrderParameter orderParameter : getOrderParameters()) {
                            resolveQuery.addOrderParameter(orderParameter);
                        }

                        copyEvaluators(resolveQuery, this);
                        data = dataSource.getResourceData(verifyInstance(resolveQuery, consumer));
                    }
                }
                timeCollectingData = System.currentTimeMillis() - timeCollectingData;

                //Filtering data
                boolean add;

                //Collect all the aggregate functions into the array list.
                List<String> returnParametersAsArray = new ArrayList<>();
                for (QueryReturnParameter returnParameter : getReturnParameters()) {
                    if(returnParameter instanceof QueryReturnFunction && ((QueryReturnFunction)returnParameter).isAggregate()) {
                        aggregateFunctions.add((QueryReturnFunction) returnParameter);
                    }
                    returnParametersAsArray.add(returnParameter.getAlias());
                }

                StringBuilder hashCode;
                if (!groupParameters.isEmpty()) {
                    if(isDisjoint()) {
                        disjointResultSets = new HashMap<>();
                    } else {
                        groupables = new HashMap<>();
                    }
                }

                for (O object : data) {
                    Long timeEvaluating = System.currentTimeMillis();
                    add = verifyCondition(object, dataSource, consumer);
                    timeEvaluating = System.currentTimeMillis() - timeEvaluating;
                    timeEvaluatingConditions += timeEvaluating;
                    evaluatingCount++;
                    if (add) {
                        Long timeFormatting = System.currentTimeMillis();
                        if (object instanceof Enlarged || object instanceof Map) {
                            Enlarged enlargedObject;
                            if(object instanceof Enlarged) {
                                if (returnAll) {
                                    enlargedObject = ((Enlarged) object).clone();
                                    presentFields.addAll(enlargedObject.keySet());
                                } else {
                                    enlargedObject = ((Enlarged) object).clone(returnParametersAsArray.toArray(new String[]{}));
                                }
                            } else {
                                if (returnAll) {
                                    enlargedObject = (new JoinableMap((Map<String, Object>) object)).clone();
                                    presentFields.addAll(enlargedObject.keySet());
                                } else {
                                    enlargedObject = (new JoinableMap((Map<String, Object>) object)).clone(returnParametersAsArray.toArray(new String[]{}));
                                }
                            }
                            object = (O) enlargedObject;
                            for (QueryReturnParameter returnParameter : getReturnParameters()) {
                                Map.Entry<String,Object> entry =
                                        consumer.resolveQueryReturnParameter(returnParameter, object, dataSource);
                                if(entry != null && !entry.getKey().isBlank()) {
                                    presentFields.add(entry.getKey());
                                    enlargedObject.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }

                        if (!groupParameters.isEmpty() && (object instanceof Groupable || isDisjoint())) {
                            hashCode = new StringBuilder();
                            Object groupValue;
                            Map<String,Object> groupKeyValues = new HashMap<>();
                            for (QueryReturnParameter returnParameter : groupParameters) {
                                if (returnParameter instanceof QueryReturnField) {
                                    groupValue = consumer.get(object, ((QueryReturnField) returnParameter), dataSource);
                                } else {
                                    groupValue = consumer.resolveFunction(((QueryReturnFunction) returnParameter), object, dataSource);
                                }
                                groupKeyValues.put(returnParameter.getAlias(), groupValue);
                                hashCode.append(groupValue);
                            }
                            if(isDisjoint()) {
                                Collection<Object> resultSet;
                                if(disjointResultSets.containsKey(hashCode.toString())) {
                                    resultSet = Introspection.resolve(disjointResultSets, hashCode.toString(), DISJOINT_RESULT_SET);
                                } else {
                                    resultSet = new ArrayList<>();
                                    JoinableMap disjointMap = new JoinableMap();
                                    disjointMap.putAll(groupKeyValues);
                                    disjointMap.put(DISJOINT_RESULT_SET, resultSet);
                                    disjointResultSets.put(hashCode.toString(), disjointMap);
                                }
                                if(object instanceof Enlarged && !returnAll) {
                                    ((Enlarged)object).purge();
                                }
                                resultSet.add(object);
                            } else {
                                if (groupables.containsKey(hashCode.toString())) {
                                    groupables.get(hashCode.toString()).group((Groupable) object);
                                } else {
                                    groupables.put(hashCode.toString(), (Groupable) object);
                                }
                            }
                        } else {
                            result.add(object);
                        }
                        timeFormatting = System.currentTimeMillis() - timeFormatting;
                        formattingCount++;
                        timeFormattingData += timeFormatting;
                    }
                }

                if(groupables != null) {
                    result.addAll((Collection<? extends O>) groupables.values());
                }
                if(disjointResultSets != null) {
                    result.addAll((Collection<? extends O>) disjointResultSets.values());
                }
            } finally {
                clearEvaluatorsCache();
            }

            Long timeAggregatingData = System.currentTimeMillis();
            if(aggregateFunctions.size() > 0) {
                for (QueryReturnFunction function : aggregateFunctions) {
                    result = consumer.resolveFunction(function, result, dataSource);
                }
            }

            if(result.size() > 0 && result.iterator().next() instanceof Enlarged
                    && !returnAll && !isDisjoint()) {
                result.forEach(O -> ((Enlarged)O).purge());
            }

            if(getStart() != 0 || getLimit() != null) {
                if (getLimit() != null) {
                    result = result.stream().skip(getStart()).limit(getLimit()).collect(Collectors.toList());
                } else {
                    result = result.stream().skip(getStart()).collect(Collectors.toList());
                }
            }
            timeAggregatingData = System.currentTimeMillis() - timeAggregatingData;
            totalTime = System.currentTimeMillis() - totalTime;

            ResultSet<O> resultSet = new ResultSet<>(totalTime, timeCollectingData,
                    timeEvaluatingConditions,
                    evaluatingCount == 0 ? 0 :timeEvaluatingConditions / evaluatingCount,
                    timeFormattingData,
                    formattingCount == 0 ? 0 :timeFormattingData / formattingCount,
                    timeAggregatingData,
                    presentFields,
                    result);
            result = resultSet;

            for(Queryable queryable : unions) {
                result.addAll(queryable.evaluate(dataSource, consumer));
            }
        }

        return result;
    }

    /**
     * Resolves dynamic resource and returns a collection with enlarged objects.
     * @param resource Dynamic resource instance.
     * @return Result set.
     */
    private Collection<? extends Joinable> resolveDynamicResource(QueryDynamicResource resource
            , Queryable.DataSource<Joinable> dataSource, Queryable.Consumer<Joinable> consumer) {
        Query resourceQuery = resource.getQuery();
        Map<String,Object> originalEnvironment = resourceQuery.getEnvironment();
        Map<String,Object> newEnvironment;
        if(originalEnvironment != null) {
            newEnvironment = new HashMap<>(originalEnvironment);
        } else {
            newEnvironment = new HashMap<>();
        }
        if(getEnvironment() != null) {
            newEnvironment.putAll(getEnvironment());
        }
        resourceQuery.setEnvironment(newEnvironment);
        Collection<Joinable> data = resourceQuery.evaluate(dataSource, consumer);
        resourceQuery.setEnvironment(originalEnvironment);

        if(resource.getPath() != null && !resource.getPath().isBlank()) {
            Collection resultPath = resolveResourcePath(data, resource.getPath());
            data = new ArrayList<>();
            for(Object dataObject : resultPath) {
                data.add(new JoinableMap(Introspection.toMap(dataObject)));
            }
        }
        return data;
    }

    /**
     * Resolve the introspection over the result set making union of the fields values.
     * @param resultSet Result set to make introspection.
     * @param path Path in order to found the value.
     * @return Returns the union of all the values.
     */
    private Collection resolveResourcePath(Collection resultSet, String path) {
        Collection result = new ArrayList<>();
        Object pathValue;
        for(Object row : resultSet) {
            pathValue = Introspection.resolve(row, path);
            if(pathValue != null) {
                if (pathValue instanceof Collection) {
                    result.addAll(((Collection) pathValue));
                } else {
                    result.add(pathValue);
                }
            }
        }
        return result;
    }

    private Queryable verifyInstance(Query query, Consumer consumer) {
        Queryable result = query;
        if(consumer instanceof ParameterizedQuery.ParameterizedConsumer &&
                ((ParameterizedQuery.ParameterizedConsumer)consumer).getParameters().size() > 0) {
            ParameterizedQuery parameterizedQuery = query.getParameterizedQuery();
            for(Object parameter : ((ParameterizedQuery.ParameterizedConsumer)consumer).getParameters()) {
                parameterizedQuery.add(parameter);
            }
            result = parameterizedQuery;
        }
        return result;
    }

    /**
     * This method verify if the conditions of the query are true or not.
     * @param object Object to use as condition parameters.
     * @return Returns if the evaluation of conditions are true or false in otherwise.
     */
    public final boolean verifyCondition(Object object) {
        Consumer consumer = new Queryable.IntrospectionConsumer<>();
        Collection collection = List.of(object);
        return verifyCondition(object, Q->collection, consumer);
    }

    /**
     * This method verify if the conditions of the query are true or not.
     * @param object Object to use as condition parameters.
     * @param dataSource Data source.
     * @param consumer Consumer.
     * @return Returns if the evaluation of conditions are true or false in otherwise.
     */
    public final boolean verifyCondition(Object object, DataSource dataSource, Consumer consumer) {
        Boolean result = true;
        for (Evaluator evaluator : getEvaluators()) {
            if (evaluator instanceof BooleanEvaluator &&
                    ((BooleanEvaluator) evaluator).getValue() instanceof QueryParameter &&
                    ((QueryParameter)((BooleanEvaluator) evaluator).getValue()).isUnderlying()) {
                continue;
            }
            if (!isEvaluatorDone(evaluator)) {
                result &= evaluator.evaluate(object, dataSource, consumer);
                if (!result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * This method check if the evaluator is evaluated previously into the current session.
     * @param evaluator Checking evaluator.
     * @return Return true if the evaluator is done and false in otherwise.
     */
    private boolean isEvaluatorDone(Evaluator evaluator) {
        boolean result = false;

        ServiceSession session = ServiceSession.getCurrentSession();
        if(session != null) {
            List<Evaluator> evaluatorsCache = (List<Evaluator>) session.getProperties().get(
                    SystemProperties.get(SystemProperties.Query.EVALUATORS_CACHE_NAME));
            if(evaluatorsCache != null) {
                result = evaluatorsCache.contains(evaluator);
            }
        }

        return result;
    }

    /**
     * Initialize the evaluators cache into the current session.
     */
    private void initializeEvaluatorsCache() {
        ServiceSession session = ServiceSession.getCurrentIdentity();
        if(session != null) {
            session.put(SystemProperties.get(SystemProperties.Query.EVALUATORS_CACHE_NAME),
                    new ArrayList<Evaluator>());
            session.put(SystemProperties.get(SystemProperties.Query.EVALUATOR_LEFT_VALUES_CACHE_NAME),
                    new HashMap<Evaluator, Object>());
            session.put(SystemProperties.get(SystemProperties.Query.EVALUATOR_RIGHT_VALUES_CACHE_NAME),
                    new HashMap<Evaluator, Object>());
        }
    }

    /**
     * Removes the evaluators cache of the current session.
     */
    private void clearEvaluatorsCache() {
        ServiceSession session = ServiceSession.getCurrentIdentity();
        if(session != null) {
            session.remove(SystemProperties.get(SystemProperties.Query.EVALUATORS_CACHE_NAME));
            session.remove(SystemProperties.get(SystemProperties.Query.EVALUATOR_LEFT_VALUES_CACHE_NAME));
            session.remove(SystemProperties.get(SystemProperties.Query.EVALUATOR_RIGHT_VALUES_CACHE_NAME));
        }
    }

    /**
     * This method add into the current session an instance that must be skipped of the
     * platform evaluation process.
     * @param evaluator Evaluator to skip.
     */
    public static void skipEvaluator(Evaluator evaluator) {
        ServiceSession session = ServiceSession.getCurrentSession();
        if(session != null) {
            List<Evaluator> evaluatorsCache = (List<Evaluator>) session.getProperties().get(
                    SystemProperties.get(SystemProperties.Query.EVALUATORS_CACHE_NAME));
            if(evaluatorsCache != null) {
                evaluatorsCache.add(evaluator);
            }
        }
    }

    /**
     * Copy all the evaluator from the source collection to destiny collection.
     * @param dest Destiny collection.
     * @param src Source collection.
     */
    private void copyEvaluators(EvaluatorCollection dest, EvaluatorCollection src) {
        for(Evaluator evaluator : src.getEvaluators()) {
            if(evaluator instanceof FieldEvaluator) {
                dest.addEvaluator(((FieldEvaluator)evaluator).copy());
            } else if(evaluator instanceof BooleanEvaluator) {
                BooleanEvaluator booleanEvaluator = (BooleanEvaluator) evaluator;
                if(booleanEvaluator.getValue() instanceof QueryFunction) {
                    QueryFunction queryFunction = (QueryFunction) booleanEvaluator.getValue();
                    if(queryFunction.isUnderlying()) {
                        dest.addEvaluator(evaluator);
                    }
                }
            } else if(evaluator instanceof And) {
                copyEvaluators(dest.and(), (EvaluatorCollection) evaluator);
            } else if(evaluator instanceof Or) {
                copyEvaluators(dest.or(), (EvaluatorCollection) evaluator);
            }
        }
    }

    private Collection<? extends Joinable> setResource(Collection<? extends Joinable> resultSet, String resourceName) {
        for(Joinable joinable : resultSet) {
            if(joinable instanceof  JoinableMap) {
                ((JoinableMap)joinable).setResource(resourceName);
            }
        }
        return resultSet;
    }

    /**
     * Evaluates the join operation.
     * @param dataSource Data source instance.
     * @param consumer Consumer instance.
     * @return Collection that is the result of the join operation.
     */
    private Collection<? extends Joinable> join(Queryable.DataSource<Joinable> dataSource, Queryable.Consumer<Joinable> consumer) {
        Query query = new Query(getResource());
        query.setEnvironment(getEnvironment());
        query.addReturnField(SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL));
        query.setUnderlyingFunctions(getUnderlyingFunctions(getResourceName()));
        for (Evaluator evaluator : getEvaluatorsFromResource(this, query, query.getResource())) {
            query.addEvaluator(evaluator);
        }

        Collection<? extends Joinable> rightData;
        Collection<? extends Joinable> leftData = getJoinData(query, dataSource, consumer);

        for(Join join : getJoins()) {
            // Creates the first query for the original resource.
            query = new Query(join.getResource());
            query.setEnvironment(getEnvironment());
            query.addReturnField(SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL));
            query.setUnderlyingFunctions(getUnderlyingFunctions(join.getResourceName()));

            // This loop checks the 'on' clause and copy the evaluators in the sub query to resolve join.
            for (Evaluator evaluator : optimizeJoin(leftData, join)) {
                if(join.getResource() instanceof QueryDynamicResource) {
                    ((QueryDynamicResource)join.getResource()).getQuery().addEvaluator(evaluator);
                } else {
                    query.addEvaluator(evaluator);
                }
            }

            // This loop check the 'where' clause and copy the evaluators for each resource.
            for (Evaluator evaluator : getEvaluatorsFromResource(this, query, join.getResource())) {
                query.addEvaluator(evaluator);
            }
            rightData = getJoinData(query, dataSource, consumer);
            leftData = product(leftData, rightData, join, dataSource, consumer);
        }
        return leftData;
    }

    /**
     * Get the data for each part of the join.
     * @param query Query associated to the join.
     * @param dataSource Data source instance.
     * @param consumer Consumer instance.
     * @return Returns the result set.
     */
    private Collection<? extends Joinable> getJoinData(Query query, Queryable.DataSource<Joinable> dataSource, Queryable.Consumer<Joinable> consumer) {
        Collection<? extends Joinable> result;
        if(query.getResource() instanceof  QueryDynamicResource) {
            Query dynamicResourceQuery = ((QueryDynamicResource)query.getResource()).getQuery();
            dynamicResourceQuery.setUnderlyingFunctions(getUnderlyingFunctionsAndChangeName(query.getResourceName(), dynamicResourceQuery.getResourceName()));
            result = resolveDynamicResource((QueryDynamicResource) query.getResource(), dataSource, consumer);
        } else if(getResource() instanceof QueryJsonResource) {
            result = ((QueryJsonResource)getResource()).getResourceValues();
        } else {
            query.setUnderlyingFunctions(getUnderlyingFunctions(query.getResourceName()));
            result = dataSource.getResourceData(verifyInstance(query, consumer));
        }
        return setResource(result, query.getResourceName());
    }

    /**
     * This method analyze the join structure and creates a set of evaluators in order to improve the performance of
     * the sub queries used to select the objects of the right resource of the join.
     * @param leftData Collection with the left data.
     * @param join Join structure.
     * @return Returns a set of the new filters in order to reduce the information of the right data.
     */
    private Collection<Evaluator> optimizeJoin(Collection<? extends Joinable> leftData, Join join) {
        Collection<Evaluator> result = new ArrayList<>();

        if(join.getType().equals(Join.JoinType.JOIN) ||
                join.getType().equals(Join.JoinType.INNER) ||
                join.getType().equals(Join.JoinType.LEFT)) {
            if(join.getEvaluators().size() == 1) {
                if(join.getEvaluators().stream().findFirst().get() instanceof Equals) {
                    //the join was identified with only one equality (...ON resource1.field = resource2.field)
                    Equals equals = (Equals) join.getEvaluators().stream().findFirst().get();
                    if(equals.getLeftValue() instanceof QueryField && equals.getRightValue() instanceof QueryField) {
                        QueryField foreignKey = null;
                        QueryField key = null;
                        if (!((QueryField) equals.getLeftValue()).getResource().equals(join.getResource()) &&
                                ((QueryField) equals.getRightValue()).getResource().equals(join.getResource())) {
                            foreignKey = (QueryField) equals.getLeftValue();
                            key = getQueryField(join, (QueryField) equals.getRightValue());
                        } else if (!((QueryField) equals.getRightValue()).getResource().equals(join.getResource()) &&
                                ((QueryField) equals.getLeftValue()).getResource().equals(join.getResource())) {
                            foreignKey = (QueryField) equals.getRightValue();
                            key = getQueryField(join,(QueryField) equals.getLeftValue());
                        }
                        if(foreignKey != null && key != null) {
                            Collection<Object> reducerList = new HashSet<>();
                            for(Object currentObject : leftData) {
                                Object foreignKeyValue = Introspection.resolve(currentObject, foreignKey.getFieldPath());
                                if(foreignKeyValue != null) {
                                    reducerList.add(foreignKeyValue);
                                }
                            }
                            In inEvaluator;
                            if(join.getResource() instanceof QueryDynamicResource) {
                                QueryField queryField = new QueryField(key.getContainer(), key.getFieldPath());
                                inEvaluator = new In(queryField, reducerList);
                            } else {
                                inEvaluator = new In(key, reducerList);
                            }
                            result.add(inEvaluator);
                        }
                    }
                }
            }
        }
        return result;
    }

    public QueryField getQueryField(Join join, QueryField key) {
        QueryField result = null;
        if(join.getResource() instanceof QueryDynamicResource) {
            List<QueryReturnParameter> parameters = ((QueryDynamicResource) join.getResource()).getQuery().getReturnParameters();
            for(QueryReturnParameter parameter : parameters) {
                if(parameter instanceof QueryReturnField) {
                    /**
                     * If fieldPath of Key its equals to some QueryReturnField or
                     * the alias of the QueryReturnField, return his fieldPath (Field name)
                     */
                    if((((QueryReturnField) parameter).getFieldPath().equals(key.getFieldPath())) ||
                            (parameter.getAlias() != null && !parameter.getAlias().isBlank() && parameter.getAlias().equals(key.getFieldPath()))) {
                        result = new QueryField(key.getContainer(), ((QueryReturnField) parameter).getFieldPath());
                        break;
                    }
                }
            }
        } else {
            result = key;
        }
        return result;
    }

    /**
     * Evaluates the join and creates the product of the intersection between the first resource and the second resource.
     * @param left Left data to the product.
     * @param right Right data to the product.
     * @param join Join object to evaluate the kind and the evaluators of the product.
     * @param dataSource Datasource instance.
     * @param consumer Consumer instance.
     * @return Collection that is the result of the join operation.
     */
    private Collection<Joinable> product(Collection<? extends Joinable> left, Collection<? extends Joinable> right, Join join,
                                         Queryable.DataSource<? extends Joinable> dataSource, Queryable.Consumer<? extends Joinable> consumer) {

        Collection<Joinable> leftCopy = null;
        Collection<Joinable> rightCopy = null;
        switch (join.getType()) {
            case LEFT: {
                leftCopy = new ArrayList<>();
                leftCopy.addAll(left);
                break;
            }
            case RIGHT: {
                rightCopy = new ArrayList<>();
                rightCopy.addAll(right);
                break;
            }
            case FULL: {
                leftCopy = new ArrayList<>();
                leftCopy.addAll(left);
                rightCopy = new ArrayList<>();
                rightCopy.addAll(right);
                break;
            }
        }

        Collection<Joinable> result = new ArrayList<>();
        Joinable row;
        Boolean rowEvaluation;
        for(Joinable leftJoinable : left) {
            for(Joinable rightJoinable : right) {
                row = leftJoinable.join(getResourceName(), join.getResourceName(), rightJoinable);
                rowEvaluation = false;

                for(Evaluator evaluator : join.getEvaluators()) {
                    if(!(rowEvaluation = evaluator.evaluate(row, dataSource, consumer))) {
                        break;
                    }
                }

                if(join.getOuter()) {
                    rowEvaluation = !rowEvaluation;
                }

                if(rowEvaluation) {
                    result.add(row);
                    switch (join.getType()) {
                        case LEFT: {
                            leftCopy.remove(leftJoinable);
                            break;
                        }
                        case RIGHT: {
                            rightCopy.remove(rightJoinable);
                            break;
                        }
                        case FULL: {
                            leftCopy.remove(leftJoinable);
                            rightCopy.remove(rightJoinable);
                            break;
                        }
                    }
                }
            }
        }

        switch (join.getType()) {
            case LEFT: {
                result.addAll(leftCopy);
                break;
            }
            case RIGHT: {
                result.addAll(rightCopy);
                break;
            }
            case FULL: {
                result.addAll(leftCopy);
                result.addAll(rightCopy);
                break;
            }
        }

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
        QueryParameter queryParameter;
        for(Evaluator evaluator : collection.getEvaluators()) {
            if(evaluator instanceof FieldEvaluator) {
                FieldEvaluator fieldEvaluator = (FieldEvaluator) evaluator;
                boolean evaluatorAdded = false;

                if (fieldEvaluator.getLeftValue() instanceof QueryParameter) {
                    queryParameter = (QueryParameter) fieldEvaluator.getLeftValue();
                    if(queryParameter.verifyResource(resource)) {
                        result.add(evaluator);
                        evaluatorAdded = true;
                    }
                }

                if (!evaluatorAdded) {
                    if (fieldEvaluator.getRightValue() instanceof QueryParameter) {
                        queryParameter = (QueryParameter) fieldEvaluator.getRightValue();
                        if(queryParameter.verifyResource(resource)) {
                            result.add(evaluator);
                        }
                    }
                }
            } else if(evaluator instanceof BooleanEvaluator) {
                if(((BooleanEvaluator)evaluator).getValue() instanceof QueryParameter) {
                    queryParameter = (QueryParameter) ((BooleanEvaluator)evaluator).getValue();
                    if(queryParameter.verifyResource(resource)) {
                        result.add(evaluator);
                    }
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
     * Return a copy of this query without all the evaluator and order fields of the
     * parameter collections.
     * @param evaluatorsToRemove Evaluators to optimizeJoin.
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


    public final Query reduceFieldEvaluator(String fieldName, Class<? extends FieldEvaluator>... evaluatorType) {
        return reduce(getFieldEvaluators(fieldName, evaluatorType));
    }

    /**
     * Reduce recursively all the collection into the query.
     * @param collection Collection to optimizeJoin.
     * @param evaluatorsToRemove Evaluator to remove.
     */
    private final void reduceCollection(EvaluatorCollection collection, Collection<Evaluator> evaluatorsToRemove) {
        for(Evaluator evaluatorToRemove : evaluatorsToRemove) {
            collection.removeEvaluator(evaluatorToRemove);
            collection.addEvaluator(new TrueEvaluator());
        }

        for(Evaluator evaluator : collection.getEvaluators()) {
            if(evaluator instanceof Or || evaluator instanceof And) {
                reduceCollection((EvaluatorCollection)evaluator, evaluatorsToRemove);
            }
        }
    }

    /**
     * Creates a string representation of the query object.
     * @param serializerName Serializer strategy name
     * @return String representation.
     */
    public String toString(String serializerName) {
        QuerySerializer querySerializer = Layers.get(QuerySerializer.class, serializerName);
        return querySerializer.serialize(this);
    }

    /**
     * Creates a string representation of the query object.
     * @return String representation.
     */
    @Override
    public String toString() {
        return toString(SystemProperties.get(SystemProperties.Query.DEFAULT_SERIALIZER));
    }

    /**
     * Evaluates the query using a readable data source.
     * @param query Query to evaluate.
     * @return Collections of joinable map instances.
     */
    public static Collection<JoinableMap> evaluate(String query) {
        return evaluate(compile(query));
    }

    /**
     * Evaluates the query using a readable data source.
     * @param queryable Query to evaluate.
     * @return Collections of joinable map instances.
     */
    public static Collection<JoinableMap> evaluate(Queryable queryable) {
        return queryable.evaluate(new Queryable.ReadableDataSource());
    }

    /**
     * This method evaluate if the uuid instance is a uuid type 5 and contains
     * some name of the registered resource and invoke the read method of the resource.
     * @param uuid Resource id.
     * @param <O> Expected data type.
     * @return Resource instance.
     */
    public static <O extends Object> O evaluate(UUID uuid) {
        String resourceName = NamedUuid.getName(uuid);
        IdentifiableLayerInterface identifiableLayerInterface = Layers.get(IdentifiableLayerInterface.class, resourceName);
        return (O) identifiableLayerInterface.read(uuid);
    }

    /**
     * Create a query instance from sql definition.
     * @param queryDefinition Sql definition.
     * @return Query instance.
     */
    public static Query compile(String queryDefinition) {
        return compile(queryDefinition, SystemProperties.get(SystemProperties.Query.DEFAULT_COMPILER));
    }

    /**
     *
     * @param queryDefinition String
     * @param compilerName String
     * @return compile Query
     */
    public static Query compile(String queryDefinition, String compilerName) {
        QueryCompiler queryCompiler = Layers.get(QueryCompiler.class, compilerName);
        return queryCompiler.compile(queryDefinition);
    }

    /**
     * This method is a particular implementation to create a bson document from a query instance.
     * @return Returns a bson document.
     */
    @Override
    public BsonDocument toBson() {
        BsonDocument document = new BsonDocument();
        document.put(PARCELABLE_CLASS_NAME, getClass().getName());
        document.put(QUERY_BSON_FIELD_NAME, toString());
        return document;
    }

    /**
     * This particular implementation do nothing to populate the instance.
     * @param document Bson document to populate the parcelable.
     * @param <P> Expected bson parcelable type.
     * @return Returns the same instance.
     */
    @Override
    public <P extends BsonParcelable> P populate(BsonDocument document) {
        return (P) this;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Query) && obj.toString().equals(toString());
    }

}
