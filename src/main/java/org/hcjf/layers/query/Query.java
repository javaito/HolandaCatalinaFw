package org.hcjf.layers.query;

import org.hcjf.bson.BsonDocument;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.IdentifiableLayerInterface;
import org.hcjf.layers.query.evaluators.*;
import org.hcjf.layers.query.functions.*;
import org.hcjf.layers.query.model.*;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.service.ServiceThread;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.LruMap;
import org.hcjf.utils.NamedUuid;
import org.hcjf.utils.Strings;
import org.hcjf.utils.bson.BsonParcelable;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class contains all the parameter needed to create a query.
 * This kind of queries works over any data collection.
 * @author javaito
 */
public class Query extends EvaluatorCollection implements Queryable {

    public static final String QUERY_BSON_FIELD_NAME = "__query__";
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
    private boolean returnAll;
    //private String stringRepresentation;

    static {
        //Init query compiler cache
        cache = new LruMap<>(SystemProperties.getInteger(SystemProperties.Query.COMPILER_CACHE_SIZE));

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

        //Publishing default aggregate function layers...
        Layers.publishLayer(CountQueryAggregateFunctionLayer.class);
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
     * Add a name of the field for group the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param groupField Name of the pair getter/setter.
     * @return Return the same instance of this class.
     */
    public final Query addGroupField(String groupField) {
        return addGroupField(new QueryReturnField(this, groupField));
    }

    /**
     * Add a name of the field for group the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param groupField Name of the pair getter/setter.
     * @return Return the same instance of this class.
     */
    public final Query addGroupField(QueryReturnParameter groupField) {
        groupParameters.add((QueryReturnParameter)checkQueryParameter((QueryParameter) groupField));
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
        return addOrderParameter(new QueryOrderField(this, orderField, desc));
    }

    /**
     * Add a name of the field for order the data collection. This name must be exist
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
     * Add the name of the field to be returned in the result set.
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
     * Add the name of the field to be returned in the result set.
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
                //If the query has not order fields then creates a linked hash set to
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
                        data = (Collection<O>) resolveDynamicResource((QueryDynamicResource)getResource());
                    } else {
                        //Creates the first query for the original resource.
                        Query resolveQuery = new Query(getResource());
                        resolveQuery.returnAll = true;

                        resolveQuery.setLimit(getLimit());
                        resolveQuery.setUnderlyingLimit(getUnderlyingLimit());
                        resolveQuery.setStart(getStart());
                        resolveQuery.setUnderlyingStart(getUnderlyingStart());
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
                Groupable groupable;
                if (!groupParameters.isEmpty()) {
                    groupables = new HashMap<>();
                }

                for (O object : data) {
                    Long timeEvaluating = System.currentTimeMillis();
                    add = verifyCondition(object, dataSource, consumer);
                    timeEvaluating = System.currentTimeMillis() - timeEvaluating;
                    timeEvaluatingConditions += timeEvaluating;
                    evaluatingCount++;
                    if (add) {
                        Long timeFormatting = System.currentTimeMillis();
                        if (object instanceof Enlarged) {
                            Enlarged enlargedObject;
                            if(returnAll) {
                                enlargedObject = ((Enlarged) object).clone();
                                presentFields.addAll(enlargedObject.keySet());
                            } else {
                                enlargedObject = ((Enlarged) object).clone(returnParametersAsArray.toArray(new String[]{}));
                            }
                            object = (O) enlargedObject;
                            String name;
                            Object value;
                            for (QueryReturnParameter returnParameter : getReturnParameters()) {
                                name = null;
                                value = null;
                                if (returnParameter instanceof QueryReturnField) {
                                    QueryReturnField returnField = (QueryReturnField) returnParameter;
                                    name = returnField.getAlias();
                                    value = consumer.get((O) enlargedObject, returnField, dataSource);
                                } else if (returnParameter instanceof QueryReturnFunction && !((QueryReturnFunction)returnParameter).isAggregate()) {
                                    QueryReturnFunction function = (QueryReturnFunction) returnParameter;
                                    name = function.getAlias();
                                    value = consumer.resolveFunction(function, enlargedObject, dataSource);
                                }
                                if(name != null && value != null) {
                                    presentFields.add(name);
                                    enlargedObject.put(name, value);
                                }
                            }
                        }

                        if (!groupParameters.isEmpty() && object instanceof Groupable) {
                            groupable = (Groupable) object;
                            hashCode = new StringBuilder();
                            Object groupValue;
                            for (QueryReturnParameter returnParameter : groupParameters) {
                                if (returnParameter instanceof QueryReturnField) {
                                    groupValue = consumer.get(object, ((QueryReturnField) returnParameter), dataSource);
                                } else {
                                    groupValue = consumer.resolveFunction(((QueryReturnFunction) returnParameter), object, dataSource);
                                }
                                if(groupValue == null) {
                                    hashCode.append(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL).hashCode());
                                } else {
                                    hashCode.append(groupValue.hashCode());
                                }
                            }
                            if (groupables.containsKey(hashCode.toString())) {
                                groupables.get(hashCode.toString()).group(groupable);
                            } else {
                                groupables.put(hashCode.toString(), groupable);
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
            } finally {
                clearEvaluatorsCache();
            }

            Long timeAggregatingData = System.currentTimeMillis();
            if(aggregateFunctions.size() > 0) {
                for (QueryReturnFunction function : aggregateFunctions) {
                    result = consumer.resolveFunction(function, result, dataSource);
                }
            }

            if(result.size() > 0 && result.iterator().next() instanceof Enlarged && !returnAll) {
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
        }

        return result;
    }

    /**
     * Resolves dynamic resource and returns a collection with enlarged objects.
     * @param resource Dynamic resource instance.
     * @return Result set.
     */
    private Collection<JoinableMap> resolveDynamicResource(QueryDynamicResource resource) {
        Collection<JoinableMap> data = Query.evaluate(resource.getQuery());

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
     * @return Returns if the evaluation of conditions are true or false in the otherwise.
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
     * @return Returns if the evaluation of conditions are true or false in the otherwise.
     */
    private boolean verifyCondition(Object object, DataSource dataSource, Consumer consumer) {
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
     * @return Return true if the evaluator is done and false in the otherwise.
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
        query.addReturnField(SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL));
        for (Evaluator evaluator : getEvaluatorsFromResource(this, query, query.getResource())) {
            query.addEvaluator(evaluator);
        }

        Collection<? extends Joinable> rightData;
        Collection<? extends Joinable> leftData = getJoinData(query, dataSource, consumer);

        for(Join join : getJoins()) {
            //Creates the first query for the original resource.
            query = new Query(join.getResource());
            query.addReturnField(SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL));
            for (Evaluator evaluator : optimizeJoin(leftData, join)) {
                query.addEvaluator(evaluator);
            }
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
            result = resolveDynamicResource((QueryDynamicResource) query.getResource());
        } else {
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
                            key = (QueryField) equals.getRightValue();
                        } else if (!((QueryField) equals.getRightValue()).getResource().equals(join.getResource()) &&
                                ((QueryField) equals.getLeftValue()).getResource().equals(join.getResource())) {
                            foreignKey = (QueryField) equals.getRightValue();
                            key = (QueryField) equals.getLeftValue();
                        }
                        if(foreignKey != null) {
                            Collection reducerList = new HashSet();
                            for(Object currentObject : leftData) {
                                Object foreignKeyValue = Introspection.resolve(currentObject, foreignKey.getFieldPath());
                                if(foreignKeyValue != null) {
                                    reducerList.add(foreignKeyValue);
                                }
                            }
                            In inEvaluator = new In(key, reducerList);
                            result.add(inEvaluator);
                        }
                    }
                }
            }
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
     * @return String representation.
     */
    @Override
    public synchronized String toString() {
//        if(stringRepresentation == null) {
            Strings.Builder resultBuilder = new Strings.Builder();

            //Print select
            resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT));
            resultBuilder.append(Strings.WHITE_SPACE);
            if (returnAll) {
                resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.RETURN_ALL));
                SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR);
                resultBuilder.append(Strings.WHITE_SPACE);
            }
            for (QueryReturnParameter field : getReturnParameters()) {
                resultBuilder.append(field);
                if (field.getAlias() != null) {
                    resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.AS));
                    resultBuilder.append(Strings.WHITE_SPACE).append(field.getAlias());
                }
                resultBuilder.append(Strings.EMPTY_STRING, SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR));
            }
            resultBuilder.cleanBuffer();

            //Print from
            resultBuilder.append(Strings.WHITE_SPACE);
            resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.FROM));
            resultBuilder.append(Strings.WHITE_SPACE);
            resultBuilder.append(getResource().toString());
            resultBuilder.append(Strings.WHITE_SPACE);

            //Print joins
            for (Join join : joins) {
                if (!(join.getType() == Join.JoinType.JOIN)) {
                    resultBuilder.append(join.getType());
                    resultBuilder.append(Strings.WHITE_SPACE);
                }
                resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.JOIN)).append(Strings.WHITE_SPACE);
                resultBuilder.append(join.getResource().toString()).append(Strings.WHITE_SPACE);
                resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.ON)).append(Strings.WHITE_SPACE);
                if (join.getEvaluators().size() > 0) {
                    toStringEvaluatorCollection(resultBuilder, join);
                }
            }

            if (evaluators.size() > 0) {
                resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.WHERE)).append(Strings.WHITE_SPACE);
                toStringEvaluatorCollection(resultBuilder, this);
            }

            if (groupParameters.size() > 0) {
                resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.GROUP_BY)).append(Strings.WHITE_SPACE);
                for (QueryReturnParameter groupParameter : groupParameters) {
                    resultBuilder.append(groupParameter, SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR);
                }
                resultBuilder.append(Strings.WHITE_SPACE);
                resultBuilder.cleanBuffer();
            }

            if (orderParameters.size() > 0) {
                resultBuilder.append(SystemProperties.get(SystemProperties.Query.ReservedWord.ORDER_BY)).append(Strings.WHITE_SPACE);
                for (QueryOrderParameter orderField : orderParameters) {
                    resultBuilder.append(orderField);
                    if (orderField.isDesc()) {
                        resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC));
                    }
                    resultBuilder.append(Strings.EMPTY_STRING, SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR));
                }
                resultBuilder.cleanBuffer();
            }

            if (getStart() != null) {
                resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.START));
                resultBuilder.append(Strings.WHITE_SPACE).append(getStart());
            }

            if (getUnderlyingStart() != null) {
                if(getStart() == null) {
                    resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.START)).append(Strings.WHITE_SPACE);
                }
                resultBuilder.append(Strings.ARGUMENT_SEPARATOR).append(getUnderlyingStart());
            }

            if (getLimit() != null) {
                resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT));
                resultBuilder.append(Strings.WHITE_SPACE).append(getLimit());
            }

            if (getUnderlyingLimit() != null) {
                if(getLimit() == null) {
                    resultBuilder.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT)).append(Strings.WHITE_SPACE);
                }
                resultBuilder.append(Strings.ARGUMENT_SEPARATOR).append(getUnderlyingLimit());
            }
            return resultBuilder.toString();
//        }
//
//        return stringRepresentation;
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
                if(!separator.isEmpty()) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.OR));
                }
                result.append(Strings.WHITE_SPACE);
                if(((Or)evaluator).getEvaluators().size() == 1) {
                    toStringEvaluatorCollection(result, (Or) evaluator);
                } else {
                    result.append(Strings.START_GROUP);
                    toStringEvaluatorCollection(result, (Or) evaluator);
                    result.append(Strings.END_GROUP);
                }
                result.append(Strings.WHITE_SPACE);
            } else if(evaluator instanceof And) {
                if(!separator.isEmpty()) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.AND));
                }
                result.append(Strings.WHITE_SPACE);
                if (collection instanceof Query) {
                    toStringEvaluatorCollection(result, (And) evaluator);
                } else {
                    if (((And) evaluator).getEvaluators().size() == 1) {
                        toStringEvaluatorCollection(result, (And) evaluator);
                    } else {
                        result.append(Strings.START_GROUP);
                        toStringEvaluatorCollection(result, (And) evaluator);
                        result.append(Strings.END_GROUP);
                    }
                }
                result.append(Strings.WHITE_SPACE);
            } else if(evaluator instanceof BooleanEvaluator) {
                result.append(separator);
                BooleanEvaluator booleanEvaluator = (BooleanEvaluator) evaluator;
                if(booleanEvaluator.isTrueForced()) {
                    result.append(Boolean.TRUE.toString());
                } else {
                    result = toStringFieldEvaluatorValue(booleanEvaluator.getValue(), booleanEvaluator.getClass(), result);
                }
                result.append(Strings.WHITE_SPACE);
            } else if(evaluator instanceof FieldEvaluator) {
                result.append(separator);
                FieldEvaluator fieldEvaluator = (FieldEvaluator) evaluator;
                if(fieldEvaluator.isTrueForced()) {
                    result.append(Boolean.TRUE.toString());
                } else {
                    if (fieldEvaluator.getLeftValue() == null) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL));
                    } else {
                        result = toStringFieldEvaluatorValue(fieldEvaluator.getLeftValue(), fieldEvaluator.getLeftValue().getClass(), result);
                    }
                    result.append(Strings.WHITE_SPACE);
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
                    if (fieldEvaluator.getRightValue() == null) {
                        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL));
                    } else {
                        result = toStringFieldEvaluatorValue(fieldEvaluator.getRightValue(), fieldEvaluator.getRightValue().getClass(), result);
                    }
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
        Collection<JoinableMap> result;
        Query query;
        if(queryable instanceof  Query) {
            query = (Query) queryable;
        } else {
            query = ((ParameterizedQuery)queryable).getQuery();
        }

        //if(query.getJoins().isEmpty() && query.getOrderParameters().isEmpty()) {
        //    result = Layers.get(ReadRowsLayerInterface.class, queryable.getResourceName()).readRows(queryable);
        //} else {
        //    result = queryable.evaluate(new Queryable.ReadableDataSource());
        //}
        //return result;
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
     * Creates a query with next structure 'SELECT * FROM {resourceName}'
     * @param resourceName Resource name.
     * @return Returns a single query instance.
     */
    public static Query compileSingleQuery(String resourceName) {
        return compile(String.format(SystemProperties.get(SystemProperties.Query.SINGLE_PATTERN), resourceName));
    }

    /**
     * Create a query instance from sql definition.
     * @param sql Sql definition.
     * @return Query instance.
     */
    public static Query compile(String sql) {
        return compile(sql, true);
    }

    /**
     * Create a query instance from sql definition.
     * @param sql Sql definition
     * @param ignoreCache Boolean value to indicate if the cache must be ignored or not.
     * @return Query instance.
     */
    public static Query compile(String sql, boolean ignoreCache) {
        Query result = null;
        if(!ignoreCache) {
            result = cache.get(sql);
        }
        if(result == null) {
            List<String> richTexts = Strings.groupRichText(sql);
            List<String> groups = Strings.replaceableGroup(Strings.removeLines(richTexts.get(richTexts.size() - 1)));
            result = compile(groups, richTexts, groups.size() - 1);
            if(!ignoreCache) {
                cache.put(sql, result);
            }
        }
        return result;
    }

    /**
     * Create a query instance from sql definition.
     * @param groups
     * @param startGroup
     * @return Query instance.
     */
    private static Query compile(List<String> groups, List<String> richTexts, Integer startGroup) {
        Query query;
        Pattern pattern = SystemProperties.getPattern(SystemProperties.Query.SELECT_REGULAR_EXPRESSION);
        Matcher matcher = pattern.matcher(groups.get(startGroup));

        if(matcher.matches()) {
            String selectBody = matcher.group(SystemProperties.get(SystemProperties.Query.SELECT_GROUP_INDEX));
            selectBody = selectBody.replaceFirst(Strings.CASE_INSENSITIVE_REGEX_FLAG+SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT), Strings.EMPTY_STRING);
            String fromBody = matcher.group(SystemProperties.get(SystemProperties.Query.FROM_GROUP_INDEX));
            fromBody = fromBody.replaceFirst(Strings.CASE_INSENSITIVE_REGEX_FLAG+SystemProperties.get(SystemProperties.Query.ReservedWord.FROM), Strings.EMPTY_STRING);
            String conditionalBody = matcher.group(SystemProperties.get(SystemProperties.Query.CONDITIONAL_GROUP_INDEX));
            if(conditionalBody != null && conditionalBody.endsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STATEMENT_END))) {
                conditionalBody = conditionalBody.substring(0, conditionalBody.indexOf(SystemProperties.get(SystemProperties.Query.ReservedWord.STATEMENT_END))-1);
            }

            String resourceValue = matcher.group(SystemProperties.get(SystemProperties.Query.RESOURCE_VALUE_INDEX));
            String dynamicResource = matcher.group(SystemProperties.get(SystemProperties.Query.DYNAMIC_RESOURCE_INDEX));
            String dynamicResourceAlias = matcher.group(SystemProperties.get(SystemProperties.Query.DYNAMIC_RESOURCE_ALIAS_INDEX));
            query = new Query(createResource(resourceValue, dynamicResource, dynamicResourceAlias, groups, richTexts));

            if(conditionalBody != null) {
                Pattern conditionalPatter = SystemProperties.getPattern(SystemProperties.Query.CONDITIONAL_REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
                List<String> conditionalElements = List.of(conditionalPatter.split(conditionalBody)).stream().filter(S -> !S.isBlank()).collect(Collectors.toList());
                String element;
                String elementValue;
                for (int i = 0; i < conditionalElements.size(); i++) {
                    element = conditionalElements.get(i++).trim();
                    elementValue = conditionalElements.get(i).trim();
                    if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.JOIN)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.FULL)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.INNER)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LEFT)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.RIGHT))) {

                        Join.JoinType type = Join.JoinType.valueOf(element.toUpperCase());
                        if(type != Join.JoinType.JOIN) {
                            elementValue = conditionalElements.get(++i).trim();
                        }

                        String joinConditionalBody;
                        QueryResource joinResource;
                        Pattern joinPattern = SystemProperties.getPattern(SystemProperties.Query.JOIN_REGULAR_EXPRESSION);
                        Matcher joinMatcher = joinPattern.matcher(elementValue);
                        if(joinMatcher.matches()) {
                            String joinDynamicResource = joinMatcher.group(SystemProperties.get(SystemProperties.Query.JOIN_DYNAMIC_RESOURCE_INDEX));
                            String joinResourceValue = joinMatcher.group(SystemProperties.get(SystemProperties.Query.JOIN_RESOURCE_VALUE_INDEX));
                            String joinDynamicResourceAlias = joinMatcher.group(SystemProperties.get(SystemProperties.Query.JOIN_DYNAMIC_RESOURCE_ALIAS_INDEX));
                            joinResource = createResource(joinResourceValue, joinDynamicResource, joinDynamicResourceAlias, groups, richTexts);
                            joinConditionalBody = joinMatcher.group(SystemProperties.get(SystemProperties.Query.JOIN_CONDITIONAL_BODY_INDEX));
                            joinConditionalBody = Strings.reverseGrouping(joinConditionalBody, groups);
                            joinConditionalBody = Strings.reverseRichTextGrouping(joinConditionalBody, richTexts);
                        } else {
                            throw new HCJFRuntimeException("Join syntax wrong, near '%s'", elementValue);
                        }

                        Join join = new Join(query, joinResource, type);
                        query.getResources().add(join.getResource());
                        completeEvaluatorCollection(query, joinConditionalBody, groups, richTexts, join, 0, new AtomicInteger(0));
                        query.addJoin(join);
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.WHERE))) {
                        completeEvaluatorCollection(query, elementValue, groups, richTexts, query, 0, new AtomicInteger(0));
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.ORDER_BY))) {
                        for (String orderField : elementValue.split(SystemProperties.get(
                                SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                            query.addOrderParameter((QueryOrderParameter)
                                    processStringValue(query, groups, richTexts, orderField, null, QueryOrderParameter.class, new ArrayList<>()));
                        }
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GROUP_BY))) {
                        for (String orderField : elementValue.split(SystemProperties.get(
                                SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                            query.addGroupField((QueryReturnParameter)
                                    processStringValue(query, groups, richTexts, orderField, null, QueryReturnParameter.class, new ArrayList<>()));
                        }
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT))) {
                        if(elementValue == null || elementValue.isBlank()) {
                            throw new HCJFRuntimeException("Undeclared limit value");
                        }

                        String[] limitValues = elementValue.split(Strings.ARGUMENT_SEPARATOR);
                        if(limitValues.length > 0 && !limitValues[0].isBlank()) {
                            try {
                                query.setLimit(Integer.parseInt(limitValues[0].trim()));
                            } catch (NumberFormatException ex) {
                                throw new HCJFRuntimeException("The limit value must be an integer", ex);
                            }
                        }

                        if(limitValues.length > 1 && !limitValues[1].isBlank()) {
                            try {
                                query.setUnderlyingLimit(Integer.parseInt(limitValues[1].trim()));
                            } catch (NumberFormatException ex) {
                                throw new HCJFRuntimeException("The underlying limit value must be an integer", ex);
                            }
                        }
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.START))) {
                        if(elementValue == null || elementValue.isBlank()) {
                            throw new HCJFRuntimeException("Undeclared start value");
                        }

                        String[] startValues = elementValue.split(Strings.ARGUMENT_SEPARATOR);
                        if(startValues.length > 0 && !startValues[0].isBlank()) {
                            try {
                                query.setStart(Integer.parseInt(startValues[0].trim()));
                            } catch (NumberFormatException ex) {
                                throw new HCJFRuntimeException("The start value must be an integer", ex);
                            }
                        }

                        if(startValues.length > 1 && !startValues[1].isBlank()) {
                            try {
                                query.setUnderlyingStart(Integer.parseInt(startValues[1].trim()));
                            } catch (NumberFormatException ex) {
                                throw new HCJFRuntimeException("The underlying start value must be an integer", ex);
                            }
                        }
                    }
                }
            }

            for(String returnField : selectBody.split(SystemProperties.get(
                    SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                query.addReturnField((QueryReturnParameter)
                        processStringValue(query, groups, richTexts, returnField, null, QueryReturnParameter.class, new ArrayList<>()));
            }
        } else {
            String value = groups.get(startGroup);
            int place = Strings.getNoMatchPlace(matcher, groups.get(startGroup));
            String nearFrom = Strings.getNearFrom(value, place, 5);
            throw new HCJFRuntimeException("Query match fail near from ( '...%s...' ), query body: '%s'", nearFrom, value);
        }

        return query;
    }

    /**
     * Creates the resource implementation depends of the values.
     * @param resourceValue Resource value definition.
     * @param dynamicResource Dynamic resource value.
     * @param dynamicResourceAlias Dynamic resource alias.
     * @param groups Groups collection.
     * @param richTexts Rich texts collection.
     * @return Returns the resource implementation.
     */
    private static QueryResource createResource(String resourceValue, String dynamicResource, String dynamicResourceAlias, List<String> groups, List<String> richTexts) {
        QueryResource result;
        if(dynamicResource.isBlank()) {
            result = new QueryResource(resourceValue.trim());
        } else {
            String path = null;
            if (resourceValue.indexOf(Strings.CLASS_SEPARATOR) > 0) {
                path = resourceValue.substring(resourceValue.indexOf(Strings.CLASS_SEPARATOR) + 1).trim();
                resourceValue = resourceValue.substring(0, resourceValue.indexOf(Strings.CLASS_SEPARATOR));
            }
            resourceValue = Strings.reverseGrouping(resourceValue, groups);
            resourceValue = Strings.reverseRichTextGrouping(resourceValue, richTexts);
            resourceValue = resourceValue.substring(1, resourceValue.length() - 1);
            Query subQuery;
            if (resourceValue.toUpperCase().startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT))) {
                subQuery = compile(resourceValue);
            } else {
                subQuery = compileSingleQuery(resourceValue);
            }
            result = new QueryDynamicResource(dynamicResourceAlias.trim(), subQuery, path);
        }
        return result;
    }

    /**
     * Complete the evaluator collections with all the evaluator definitions in the groups.
     * @param groups Where groups.
     * @param parentCollection Parent collection.
     * @param definitionIndex Definition index into the groups.
     */
    private static final void completeEvaluatorCollection(Query query, String startElement, List<String> groups, List<String> richTexts,
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
                        processDefinition(query, pendingDefinition, collection, groups, richTexts, placesIndex);
                    }
                    pendingDefinitions.clear();
                } else if(pendingDefinitions.size() > 1) {
                    throw new IllegalArgumentException("");
                }
            }
        }

        for(String pendingDefinition : pendingDefinitions) {
            if(collection != null) {
                processDefinition(query, pendingDefinition, collection, groups, richTexts, placesIndex);
            } else {
                processDefinition(query, pendingDefinition, parentCollection, groups, richTexts, placesIndex);
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
    private static void processDefinition(Query query, String definition, EvaluatorCollection collection, List<String> groups, List<String> richTexts, AtomicInteger placesIndex) {
        String[] evaluatorValues;
        Object leftValue;
        Object rightValue;
        String firstArgument;
        String secondArgument;
        String operator;
        Evaluator evaluator;

        evaluatorValues = definition.split(SystemProperties.get(SystemProperties.Query.OPERATION_REGULAR_EXPRESSION));
        if (evaluatorValues.length == 1 && definition.startsWith(Strings.REPLACEABLE_GROUP)) {
            Integer index = Integer.parseInt(definition.replace(Strings.REPLACEABLE_GROUP, Strings.EMPTY_STRING));
            completeEvaluatorCollection(query, null, groups, richTexts, collection, index, placesIndex);
        } else {
            boolean operatorDone = false;
            firstArgument = Strings.EMPTY_STRING;
            secondArgument = Strings.EMPTY_STRING;
            operator = Strings.EMPTY_STRING;
            for (String evaluatorValue : evaluatorValues) {
                evaluatorValue = evaluatorValue.trim();
                if (evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT))) {
                    operator += evaluatorValue + Strings.WHITE_SPACE;
                } else if (evaluatorValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_2))) {
                    operator += evaluatorValue;
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

            List<QueryField> presentFields = new ArrayList<>();
            if (operator == null || operator.trim().isEmpty()) {
                leftValue = processStringValue(query, groups, richTexts, firstArgument.trim(), placesIndex, QueryParameter.class, presentFields);
                evaluator = new BooleanEvaluator(leftValue);
            } else {
                leftValue = processStringValue(query, groups, richTexts, firstArgument.trim(), placesIndex, QueryParameter.class, presentFields);
                if (leftValue instanceof String) {
                    leftValue = Strings.reverseGrouping((String) leftValue, groups);
                }
                rightValue = processStringValue(query, groups, richTexts, secondArgument.trim(), placesIndex, QueryParameter.class, presentFields);
                if (rightValue instanceof String) {
                    rightValue = Strings.reverseGrouping((String) rightValue, groups);
                }
                operator = operator.trim();


                if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT)) ||
                        operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT_2))) {
                    evaluator = new Distinct(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS))) {
                    evaluator = new Equals(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN))) {
                    evaluator = new GreaterThan(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS))) {
                    evaluator = new GreaterThanOrEqual(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.IN))) {
                    evaluator = new In(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE))) {
                    evaluator = new Like(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_IN))) {
                    evaluator = new NotIn(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN))) {
                    evaluator = new SmallerThan(leftValue, rightValue);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN_OR_EQUALS))) {
                    evaluator = new SmallerThanOrEqual(leftValue, rightValue);
                } else {
                    throw new HCJFRuntimeException("Unsupported operator '%s', near '%s'", operator, definition);
                }
            }

            if(evaluator instanceof BaseEvaluator) {
                ((BaseEvaluator)evaluator).setEvaluatorFields(presentFields);
            }

            collection.addEvaluator(evaluator);
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
    private static Object processStringValue(Query query, List<String> groups, List<String> richTexts, String stringValue, AtomicInteger placesIndex, Class parameterClass, List<QueryField> presentFields) {
        Object result = null;
        String trimmedStringValue = stringValue.trim();
        if(trimmedStringValue.equals(SystemProperties.get(SystemProperties.Query.ReservedWord.REPLACEABLE_VALUE))) {
            //If the string value is equals than "?" then the value object is an instance of ReplaceableValue.
            result = new FieldEvaluator.ReplaceableValue(placesIndex.getAndAdd(1));
        } else if(trimmedStringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL))) {
            result = null;
        } else if(trimmedStringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.TRUE))) {
            result = true;
        } else if(trimmedStringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.FALSE))) {
            result = false;
        } else if(trimmedStringValue.startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER))) {
            if (trimmedStringValue.endsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER))) {
                //If the string value start and end with "'" then the value can be a string or a date object.
                trimmedStringValue = trimmedStringValue.substring(1, trimmedStringValue.length() - 1);
                trimmedStringValue = richTexts.get(Integer.parseInt(trimmedStringValue.replace(Strings.REPLACEABLE_RICH_TEXT, Strings.EMPTY_STRING)));

                //Clean the value to remove all the skip characters into the string value.
                trimmedStringValue = trimmedStringValue.replace(Strings.RICH_TEXT_SKIP_CHARACTER + Strings.RICH_TEXT_SEPARATOR, Strings.RICH_TEXT_SEPARATOR);

                try {
                    result = SystemProperties.getDateFormat(SystemProperties.Query.DATE_FORMAT).parse(trimmedStringValue);
                } catch (Exception ex) {
                    //The value is not a date then the value is a string
                    while(trimmedStringValue.contains(Strings.REPLACEABLE_GROUP)) {
                        trimmedStringValue = Strings.reverseGrouping(trimmedStringValue, groups);
                    }
                    result = trimmedStringValue;
                }
            } else {
                throw new HCJFRuntimeException("Expecting string en delimiter, near %s", trimmedStringValue);
            }
        } else if(trimmedStringValue.startsWith(Strings.REPLACEABLE_GROUP)) {
            Integer index = Integer.parseInt(trimmedStringValue.replace(Strings.REPLACEABLE_GROUP, Strings.EMPTY_STRING));
            String group = groups.get(index);
            if(group.toUpperCase().startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT))) {
                result = new FieldEvaluator.QueryValue(Query.compile(groups, richTexts, index));
            } else {
                //If the string value start with "(" and end with ")" then the value is a collection.
                Collection<Object> collection = new ArrayList<>();
                for (String subStringValue : group.split(SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                    collection.add(processStringValue(query, groups, richTexts, subStringValue.trim(), placesIndex, parameterClass, presentFields));
                }
                result = collection;
            }
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_UUID_REGEX))) {
            result = UUID.fromString(trimmedStringValue);
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_INTEGER_NUMBER_REGEX))) {
            try {
                result = Long.parseLong(trimmedStringValue);
            } catch (Exception ex) {
                result = trimmedStringValue;
            }
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_DECIMAL_NUMBER_REGEX))) {
            try {
                result = SystemProperties.getDecimalFormat(SystemProperties.Query.DECIMAL_FORMAT).parse(trimmedStringValue);
            } catch (ParseException e) {
                result = trimmedStringValue;
            }
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_SCIENTIFIC_NUMBER_REGEX))) {
            try {
                result = SystemProperties.getDecimalFormat(SystemProperties.Query.SCIENTIFIC_NOTATION_FORMAT).parse(trimmedStringValue);
            } catch (ParseException e) {
                result = trimmedStringValue;
            }
        } else if(trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_MATH_CONNECTOR_REGULAR_EXPRESSION)) &&
                trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_MATH_REGULAR_EXPRESSION))) {
            String alias = null;
            String[] asParts = trimmedStringValue.split(SystemProperties.get(SystemProperties.Query.AS_REGULAR_EXPRESSION));
            if(asParts.length == 3) {
                trimmedStringValue = asParts[0].trim();
                alias = asParts[2].trim();
            }

            //If the string matchs with a math expression then creates a function that resolves this math expression.
            String[] mathExpressionParts = trimmedStringValue.split(SystemProperties.get(SystemProperties.HCJF_MATH_SPLITTER_REGULAR_EXPRESSION));
            List<Object> parameters = new ArrayList<>();
            String currentValue;
            boolean desc = false;
            for (int i = 0; i < mathExpressionParts.length; i++) {
                currentValue = mathExpressionParts[i];
                if(i == mathExpressionParts.length - 1){
                    //This code run only one time for the last part.
                    if(parameterClass.equals(QueryReturnParameter.class)) {
                        //Check if the last part contains the 'AS' word
                        String[] parts = currentValue.split(SystemProperties.get(SystemProperties.Query.AS_REGULAR_EXPRESSION));
                        if (parts.length == 2) {
                            currentValue = parts[0].trim();
                            alias = parts[1].trim();
                        }
                    } else if(parameterClass.equals(QueryOrderParameter.class)) {
                        //Check if the last part contains the 'DESC' word
                        String[] parts = currentValue.split(SystemProperties.get(SystemProperties.Query.DESC_REGULAR_EXPRESSION));
                        if(parts.length == 3) {
                            currentValue = parts[0].trim();
                            if(parts[2].trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC))) {
                                desc = true;
                            }
                        }
                    }
                }

                if(currentValue.matches(SystemProperties.get(SystemProperties.HCJF_MATH_CONNECTOR_REGULAR_EXPRESSION))) {
                    //If the current value is a math connector (+-*/) the this connector is a function parameter.
                    parameters.add(currentValue.trim());
                } else {
                    //If the current value is not a math connector then this string is evaluated recursively.
                    parameters.add(processStringValue(query, groups, richTexts, currentValue, placesIndex, QueryParameter.class, presentFields));
                }
            }

            if(parameterClass.equals(QueryParameter.class)) {
                result = new QueryFunction(query, Strings.reverseGrouping(trimmedStringValue, groups),
                        SystemProperties.get(SystemProperties.Query.Function.MATH_EVAL_EXPRESSION_NAME), parameters);
            } else if(parameterClass.equals(QueryReturnParameter.class)) {
                result = new QueryReturnFunction(query, Strings.reverseGrouping(trimmedStringValue, groups),
                        SystemProperties.get(SystemProperties.Query.Function.MATH_EVAL_EXPRESSION_NAME), parameters, alias);
            } else if(parameterClass.equals(QueryOrderParameter.class)) {
                result = new QueryOrderFunction(query, Strings.reverseGrouping(trimmedStringValue, groups),
                        SystemProperties.get(SystemProperties.Query.Function.MATH_EVAL_EXPRESSION_NAME), parameters, desc);
            }
        } else {
            //Default case, only must be a query parameter.
            String functionName = null;
            String originalValue = null;
            String replaceValue = null;
            String group = null;
            List<Object> functionParameters = null;
            Boolean function = false;
            if(trimmedStringValue.contains(Strings.REPLACEABLE_GROUP)) {
                //If the string contains a replaceable group character then the parameter is a function.
                replaceValue = Strings.getGroupIndex(trimmedStringValue, Strings.REPLACEABLE_GROUP);
                group = groups.get(Integer.parseInt(replaceValue.replace(Strings.REPLACEABLE_GROUP,Strings.EMPTY_STRING)));
                functionName = trimmedStringValue.substring(0, trimmedStringValue.indexOf(Strings.REPLACEABLE_GROUP));
                originalValue = trimmedStringValue.replace(replaceValue, Strings.START_GROUP + group + Strings.END_GROUP);
                functionParameters = new ArrayList<>();
                for(String param : group.split(SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                    functionParameters.add(processStringValue(query, groups, richTexts, param, placesIndex, parameterClass, presentFields));
                }
                originalValue = Strings.reverseRichTextGrouping(originalValue, richTexts);
                function = true;
            } else {
                originalValue = trimmedStringValue;
            }

            if(parameterClass.equals(QueryParameter.class)) {
                //If the parameter class is the default class then the result will be a
                //QueryFunction.class instance or QueryField.class instance.
                if(function) {
                    result = new QueryFunction(query, originalValue, functionName, functionParameters);
                } else {
                    result = new QueryField(query, trimmedStringValue);
                }
            } else if(parameterClass.equals(QueryReturnParameter.class)) {
                //If the parameter class is the QueryReturnParameter.class then the result will be a
                //QueryReturnFunction.class instance or QueryReturnField.class instance.
                String alias = null;
                String[] parts = originalValue.split(SystemProperties.get(SystemProperties.Query.AS_REGULAR_EXPRESSION));
                if(parts.length == 3) {
                    originalValue = parts[0].trim();
                    alias = parts[2].trim();
                }

                if(function) {
                    result = new QueryReturnFunction(query, originalValue, functionName, functionParameters, alias);
                } else {
                    result = new QueryReturnField(query, originalValue, alias);
                }
            } else if(parameterClass.equals(QueryOrderParameter.class)) {
                //If the parameter class is the QueryOrderParameter.class then the result will be a
                //QueryOrderFunction.class instance or QueryOrderField.class instance.
                boolean desc = false;
                String[] parts = originalValue.split(SystemProperties.get(SystemProperties.Query.DESC_REGULAR_EXPRESSION));
                if(parts.length == 2) {
                    originalValue = parts[0].trim();
                    if(parts[1].trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC))) {
                        desc = true;
                    }
                }

                if(function) {
                    result = new QueryOrderFunction(query, originalValue, functionName, functionParameters, desc) ;
                } else {
                    result = new QueryOrderField(query, originalValue, desc);
                }
            }
        }

        if(result instanceof QueryField) {
            presentFields.add((QueryField) result);
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Query) && obj.toString().equals(toString());
    }

}
