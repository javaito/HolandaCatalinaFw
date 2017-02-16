package org.hcjf.layers.query;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.Strings;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains all the parameter needed to create a query.
 * This kind of queries works over any data collection.
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Query extends EvaluatorCollection {

    private final QueryId id;
    private final QueryResource resource;
    private Integer limit;
    private Object start;
    private final List<OrderField> orderFields;
    private final List<QueryReturnField> returnFields;
    private final List<Join> joins;

    public Query(String resource, QueryId id) {
        this.id = id;
        this.orderFields = new ArrayList<>();
        this.returnFields = new ArrayList<>();
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
        this.orderFields = new ArrayList<>();
        this.orderFields.addAll(source.orderFields);
        this.returnFields = new ArrayList<>();
        this.returnFields.addAll(source.returnFields);
        this.joins = new ArrayList<>();
        this.joins.addAll(source.joins);
    }

    private QueryField checkQueryField(QueryField queryField) {
        QueryResource resource = queryField.getResource();
        if(resource == null) {
            queryField.setResource(getResource());
        }
        return queryField;
    }

    @Override
    protected Evaluator checkEvaluator(Evaluator evaluator) {
        if(evaluator instanceof FieldEvaluator) {
            checkQueryField(((FieldEvaluator)evaluator).getQueryField());
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
    public final Object getStart() {
        return start;
    }

    /**
     * Set the first object of the result.
     * @param start First object of the result.
     */
    public final void setStart(Object start) {
        this.start = start;
    }

    /**
     * Return the unmodifiable list with order fields.
     * @return Order fields.
     */
    public final List<OrderField> getOrderFields() {
        return Collections.unmodifiableList(orderFields);
    }

    /**
     * Add a name of the field for order the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param orderField Name of the pair getter/setter.
     * @return Return the same instance of this class.
     */
    public final Query addOrderField(String orderField) {
        addOrderField(orderField, SystemProperties.getBoolean(SystemProperties.Query.DEFAULT_DESC_ORDER));
        return this;
    }

    /**
     * Add a name of the field for order the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param orderField Name of the pair getter/setter.
     * @param desc Desc property.
     * @return Return the same instance of this class.
     */
    public final Query addOrderField(String orderField, boolean desc) {
        orderFields.add(new OrderField(checkQueryField(new QueryField(orderField)), desc));
        return this;
    }

    /**
     * Return an unmodifiable list with the return fields.
     * @return Return fields.
     */
    public final List<QueryField> getReturnFields() {
        return Collections.unmodifiableList(returnFields);
    }

    /**
     * Add the name of the field to be returned in the result set.
     * @param returnField Field name.
     * @return Return the same instance of this class.
     */
    public final Query addReturnField(String returnField) {
        returnFields.add((QueryReturnField) checkQueryField(new QueryReturnField(returnField)));
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
        if(orderFields.size() > 0) {
            //If the query has order fields then creates a tree set with
            //a comparator using the order fields.
            result = new TreeSet<>((o1, o2) -> {
                int compareResult = 0;

                Comparable<Object> comparable1;
                Comparable<Object> comparable2;
                for (OrderField orderField : orderFields) {
                    try {
                        comparable1 = consumer.get(o1, orderField.getQueryField().getFieldName());
                        comparable2 = consumer.get(o2, orderField.getQueryField().getFieldName());
                    } catch (ClassCastException ex) {
                        throw new IllegalArgumentException("Order field must be comparable");
                    }

                    if(comparable1 == null ^ comparable2 == null) {
                        compareResult = (comparable1 == null) ? -1 : 1;
                    } else if(comparable1 == null && comparable2 == null) {
                        compareResult = 0;
                    } else {
                        compareResult = comparable1.compareTo(comparable2) * (orderField.isDesc() ? -1 : 1);
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

        //Getting data from data source.
        Collection<O> data;
        if(joins.size() > 0) {
            //If the query has joins then data source must return the joined data
            //collection using all the resources
            data = (Collection<O>) join((DataSource<Joinable>) dataSource, (Consumer<Joinable>) consumer);
        } else {
            //If the query has not joins then data source must return data from
            //resource of the query.
            data = dataSource.getResourceData(this);
        }

        //Filtering data
        boolean add;
        for(O object : data) {
            add = true;
            for(Evaluator evaluator : getEvaluators()) {
                add = evaluator.evaluate(object, dataSource, consumer, parameters);
            if(!add) {
                    break;
                }
            }
            if(add) {
                result.add(object);
            }
            if(getLimit() != null && result.size() == getLimit()) {
                break;
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
    private final Collection<Joinable> join(DataSource<Joinable> dataSource, Consumer<Joinable> consumer) {
        Collection<Joinable> result = new ArrayList<>();

        //Creates the first query for the original resource.
        Query joinQuery = new Query(getResourceName());
        joinQuery.returnFields.addAll(this.returnFields);
        for(Evaluator evaluator : getEvaluatorsFromResource(this, joinQuery, getResourceName())) {
            joinQuery.addEvaluator(evaluator);
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
            for (Evaluator evaluator : getEvaluatorsFromResource(this, joinQuery, join.getResourceName())) {
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

        //Evaluate from the start query to right
        int j = joinStart;
        for (int i = queryStart; i < queries.size(); i++) {
            joinQuery = queries.get(i);
            if(leftJoinables.isEmpty()) {
                leftJoinables.addAll(dataSource.getResourceData(joinQuery));
            } else {
                queryJoin = joins.get(j);
                indexedJoineables = index(leftJoinables, queryJoin.getLeftField().getFieldName(), consumer);
                leftJoinables.clear();
                keys = indexedJoineables.keySet();
                joinQuery.addEvaluator(new In(queryJoin.getRightField().toString(), keys));
                rightJoinables.addAll(dataSource.getResourceData(joinQuery));
                for (Joinable rightJoinable : rightJoinables) {
                    for (Joinable leftJoinable : indexedJoineables.get(rightJoinable.get(queryJoin.getRightField().getFieldName()))) {
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
            indexedJoineables = index(rightJoinables, queryJoin.getRightField().getFieldName(), consumer);
            rightJoinables.clear();
            keys = indexedJoineables.keySet();
            joinQuery.addEvaluator(new In(queryJoin.getLeftField().toString(), keys));
            leftJoinables.addAll(dataSource.getResourceData(joinQuery));
            for (Joinable leftJoinable : leftJoinables) {
                for (Joinable rightJoinable : indexedJoineables.get(leftJoinable.get(queryJoin.getLeftField().getFieldName()))) {
                    rightJoinables.add(rightJoinable.join(leftJoinable));
                }
            }
        }

        result.addAll(rightJoinables);
        return result;
    }

    /**
     *
     * @param collection
     * @param resourceName
     * @return
     */
    private List<Evaluator> getEvaluatorsFromResource(EvaluatorCollection collection, EvaluatorCollection parent, String resourceName) {
        List<Evaluator> result = new ArrayList<>();
        for(Evaluator evaluator : collection.getEvaluators()) {
            if(evaluator instanceof FieldEvaluator) {
                if(((FieldEvaluator) evaluator).getQueryField().getResource().getResourceName().equals(resourceName)) {
                    result.add(evaluator);
                }
            } else if(evaluator instanceof EvaluatorCollection) {
                EvaluatorCollection subCollection = null;
                if(evaluator instanceof And) {
                    subCollection = new And(parent);
                } else if(evaluator instanceof Or) {
                    subCollection = new Or(parent);
                }
                for(Evaluator subEvaluator : getEvaluatorsFromResource((EvaluatorCollection)evaluator, subCollection, resourceName)) {
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
    private final Map<Object, Set<Joinable>> index(Collection<Joinable> objects, String fieldIndex, Consumer<Joinable> consumer) {
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        //Print select
        result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT));
        result.append(Strings.WHITE_SPACE);
        String separator = Strings.EMPTY_STRING;
        for(QueryField field : getReturnFields()) {
            result.append(separator).append(field);
            separator = SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR);
        }

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
        }

        if(evaluators.size() > 0) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.WHERE)).append(Strings.WHITE_SPACE);
            toStringEvaluatorCollection(result, this);
        }

        if(orderFields.size() > 0) {
            result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.ORDER_BY)).append(Strings.WHITE_SPACE);
            separator = Strings.EMPTY_STRING;
            for(OrderField orderField : orderFields) {
                result.append(separator).append(orderField.getQueryField());
                if(orderField.isDesc()) {
                    result.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC));
                }
                separator = SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR);
            }
        }

        if(getLimit() != null) {
            result.append(Strings.WHITE_SPACE).append(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT));
            result.append(Strings.WHITE_SPACE).append(getLimit());
        }

        return result.toString();
    }

    private void toStringEvaluatorCollection(StringBuilder result, EvaluatorCollection collection) {
        String separator = Strings.EMPTY_STRING;
        String separatorValue = collection instanceof Or ?
                SystemProperties.get(SystemProperties.Query.ReservedWord.OR) :
                SystemProperties.get(SystemProperties.Query.ReservedWord.AND);
        for(Evaluator evaluator : collection.getEvaluators()) {
            result.append(separator);
            if(evaluator instanceof Or) {
                result.append(Strings.START_GROUP);
                toStringEvaluatorCollection(result, (Or)evaluator);
                result.append(Strings.END_GROUP);
            } else if(evaluator instanceof And) {
                if(collection instanceof Query) {
                    toStringEvaluatorCollection(result, (And) evaluator);
                } else {
                    result.append(Strings.START_GROUP);
                    toStringEvaluatorCollection(result, (And) evaluator);
                    result.append(Strings.END_GROUP);
                }
            } else if(evaluator instanceof FieldEvaluator) {
                FieldEvaluator fieldEvaluator = (FieldEvaluator) evaluator;
                result.append(fieldEvaluator.getQueryField()).append(Strings.WHITE_SPACE);
                if (fieldEvaluator instanceof Distinct) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof Equals) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof GreaterThanOrEqual) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof GreaterThan) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof In) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.IN)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof Like) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE)).append(Strings.WHITE_SPACE);
                } else if (fieldEvaluator instanceof NotIn) {
                    result.append(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_IN)).append(Strings.WHITE_SPACE);
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

    private static StringBuilder toStringFieldEvaluatorValue(Object value, Class type, StringBuilder result) {
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
                result.append(separator);
                result = toStringFieldEvaluatorValue(object, object.getClass(), result);
                separator = SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR);
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
            selectBody = selectBody.replaceFirst("(?i)"+SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT), Strings.EMPTY_STRING);
            String fromBody = matcher.group(SystemProperties.getInteger(SystemProperties.Query.FROM_GROUP_INDEX));
            fromBody = fromBody.replaceFirst("(?i)"+SystemProperties.get(SystemProperties.Query.ReservedWord.FROM), Strings.EMPTY_STRING);
            String conditionalBody = matcher.group(SystemProperties.getInteger(SystemProperties.Query.CONDITIONAL_GROUP_INDEX));
            if(conditionalBody != null && conditionalBody.endsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STATEMENT_END))) {
                conditionalBody = conditionalBody.substring(0, conditionalBody.indexOf(SystemProperties.get(SystemProperties.Query.ReservedWord.STATEMENT_END))-1);
            }

            query = new Query(fromBody.trim());

            for(String returnFields : selectBody.split(SystemProperties.get(
                    SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                query.addReturnField(returnFields);
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
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.INNER_JOIN)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LEFT_JOIN)) ||
                            element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.RIGHT_JOIN))) {
                        String[] joinElements = elementValue.split(SystemProperties.get(SystemProperties.Query.JOIN_REGULAR_EXPRESSION));
                        Join join = new Join(
                                joinElements[SystemProperties.getInteger(SystemProperties.Query.JOIN_RESOURCE_NAME_INDEX)].trim(),
                                new QueryField(joinElements[SystemProperties.getInteger(SystemProperties.Query.JOIN_LEFT_FIELD_INDEX)].trim()),
                                new QueryField(joinElements[SystemProperties.getInteger(SystemProperties.Query.JOIN_RIGHT_FIELD_INDEX)].trim()),
                                Join.JoinType.valueOf(element));
                        query.addJoin(join);
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.WHERE))) {
                        completeWhere(elementValue, groups, query, 0, new AtomicInteger(0));
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.ORDER_BY))) {
                        for (String orderField : elementValue.split(SystemProperties.get(
                                SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                            boolean desc = SystemProperties.getBoolean(SystemProperties.Query.DEFAULT_DESC_ORDER);
                            if (orderField.toUpperCase().contains(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC))) {
                                desc = true;
                                orderField = orderField.replaceAll(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC), Strings.EMPTY_STRING).trim();
                            }
                            query.addOrderField(orderField, desc);
                        }
                    } else if (element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT))) {
                        query.setLimit(Integer.parseInt(elementValue));
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
    private static final void completeWhere(String startElement, List<String> groups,
                                            EvaluatorCollection parentCollection,
                                            Integer definitionIndex,
                                            AtomicInteger placesIndex) {
        Pattern wherePatter = SystemProperties.getPattern(SystemProperties.Query.WHERE_REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
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
                    if(parentCollection instanceof Query || parentCollection instanceof And) {
                        collection = parentCollection;
                    } else {
                        collection = parentCollection.and();
                    }
                } else if (collection instanceof Or) {
                    if(parentCollection instanceof Query || parentCollection instanceof And) {
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
                } else if (collection instanceof And) {
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

    private static void processDefinition(String definition, EvaluatorCollection collection, List<String> groups, AtomicInteger placesIndex) {
        String[] evaluatorValues;
        String fieldValue;
        String operator;
        Object value;
        String stringValue;
        Evaluator evaluator = null;

        if (definition.startsWith(Strings.REPLACEABLE_GROUP)) {
            Integer index = Integer.parseInt(definition.replace(Strings.REPLACEABLE_GROUP, Strings.EMPTY_STRING));
            completeWhere(null, groups, collection, index, placesIndex);
        } else {
            evaluatorValues = definition.split(SystemProperties.get(SystemProperties.Query.OPERATION_REGULAR_EXPRESSION));
            if (evaluatorValues.length >= 3) {

                boolean operatorDone = false;
                fieldValue = "";
                stringValue = "";
                operator = null;
                for (String evaluatorValue : evaluatorValues) {
                    if (!operatorDone && (evaluatorValue.trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT))
                            || evaluatorValue.trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS))
                            || evaluatorValue.trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN))
                            || evaluatorValue.trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS))
                            || evaluatorValue.trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.IN))
                            || evaluatorValue.trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE))
                            || evaluatorValue.trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_IN))
                            || evaluatorValue.trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN))
                            || evaluatorValue.trim().equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN_OR_EQUALS)))) {
                        operator = evaluatorValue.trim();
                        operatorDone = true;
                    } else if (operatorDone) {
                        stringValue += evaluatorValue + Strings.WHITE_SPACE;
                    } else {
                        fieldValue += evaluatorValue + Strings.WHITE_SPACE;
                    }
                }

                if (operator == null) {
                    throw new IllegalArgumentException("Operator not found for expression: " + definition);
                }

                fieldValue = fieldValue.trim();
                operator = operator.trim();

                //Check the different types of parameters
                stringValue = stringValue.trim();
                value = processStringValue(groups, stringValue, placesIndex);

                if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DISTINCT))) {
                    evaluator = new Distinct(fieldValue, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.EQUALS))) {
                    evaluator = new Equals(fieldValue, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN))) {
                    evaluator = new GreaterThan(fieldValue, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.GREATER_THAN_OR_EQUALS))) {
                    evaluator = new GreaterThanOrEqual(fieldValue, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.IN))) {
                    evaluator = new In(fieldValue, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIKE))) {
                    evaluator = new Like(fieldValue, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NOT_IN))) {
                    evaluator = new NotIn(fieldValue, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN))) {
                    evaluator = new SmallerThan(fieldValue, value);
                } else if (operator.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.SMALLER_THAN_OR_EQUALS))) {
                    evaluator = new SmallerThanOrEqual(fieldValue, value);
                } else {

                }

                collection.addEvaluator(evaluator);
            } else {
                throw new IllegalArgumentException("Syntax error for expression: " + definition + ", expected {field} {operator} {value}");
            }
        }
    }

    private static Object processStringValue(List<String> groups, String stringValue, AtomicInteger placesIndex) {
        Object result;
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
            if(group.startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT))) {
                result = new FieldEvaluator.QueryValue(Query.compile(groups, index));
            } else {
                //If the string value start with "(" and end with ")" then the value is a collection.
                Collection<Object> collection = new ArrayList<>();
                for (String subStringValue : group.split(SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                    collection.add(processStringValue(groups, subStringValue, placesIndex));
                }
                result = collection;
            }
        } else if(stringValue.startsWith(Strings.START_GROUP)) {
            if (stringValue.endsWith(Strings.END_GROUP)) {
                //If the string value start with "(" and end with ")" then the value is a collection.
                Collection<Object> collection = new ArrayList<>();
                stringValue = stringValue.substring(1, stringValue.length() - 1);
                for (String subStringValue : stringValue.split(SystemProperties.get(SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                    collection.add(processStringValue(groups, subStringValue, placesIndex));
                }
                result = collection;
            } else {
                throw new IllegalArgumentException();
            }
        } else if(stringValue.matches(SystemProperties.get(SystemProperties.HCJF_UUID_REGEX))) {
            result = UUID.fromString(stringValue);
        } else {
            //The last chance is the value be a number
            try {
                result = NumberFormat.getInstance().parse(stringValue);
            } catch (ParseException e) {
                throw new IllegalArgumentException();
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
         * @param fieldName Name of particular data.
         * @return Return the data storage in the data source indexed
         * by the parameter name.
         */
        public <R extends Object> R get(O instance, String fieldName);

    }

    /**
     * This private class is the default consume method of the queries.
     */
    private static class IntrospectionConsumer<O extends Object> implements Consumer<O> {

        /**
         * Get naming information from an instance.
         *
         * @param instance    Data source.
         * @param fieldName Name of particular data.
         * @return Return the data storage in the data source indexed
         * by the parameter name.
         */
        @Override
        public <R extends Object> R get(O instance, String fieldName) {
            Object result = null;
            try {
                if(instance instanceof JoinableMap) {
                    result = ((JoinableMap)instance).get(fieldName);
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
     * This class represents a order field with desc property
     */
    public static class OrderField {

        private final QueryField queryField;
        private final boolean desc;

        public OrderField(QueryField queryField, boolean desc) {
            this.queryField = queryField;
            this.desc = desc;
        }

        /**
         * Return the query field
         * @return Query field
         */
        public QueryField getQueryField() {
            return queryField;
        }

        /**
         * Return the desc property.
         * @return Desc property.
         */
        public boolean isDesc() {
            return desc;
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
            return resourceName.equals(obj);
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

    /**
     * This class represents any kind of query fields.
     */
    public static class QueryField implements Comparable<QueryField>, QueryComponent {

        private QueryResource resource;
        private String fieldName;
        private final String index;
        private final String originalValue;

        public QueryField(String field) {
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
            this.originalValue = field;
        }

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
         * Return the index associated to the field.
         * @return Index, can be null.
         */
        public String getIndex() {
            return index;
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
            boolean result = false;
            if(obj instanceof QueryField) {
                result = toString().equals(obj.toString());
            }
            return result;
        }

        @Override
        public int compareTo(QueryField o) {
            return toString().compareTo(o.toString());
        }
    }

    /**
     * This kind of component represent the fields to be returned into the query.
     */
    public static class QueryReturnField extends QueryField {

        private final String alias;
        private final String originalValue;

        public QueryReturnField(String field) {
            super(field.contains(SystemProperties.get(SystemProperties.Query.ReservedWord.AS)) ?
                    field.substring(0, field.indexOf(SystemProperties.get(SystemProperties.Query.ReservedWord.AS))).trim() :
                    field);

            originalValue = field;
            if(field.contains(SystemProperties.get(SystemProperties.Query.ReservedWord.AS))) {
                alias = field.substring(field.indexOf(SystemProperties.get(SystemProperties.Query.ReservedWord.AS)) +
                        SystemProperties.get(SystemProperties.Query.ReservedWord.AS).length()).trim();
            } else {
                alias = null;
            }
        }

        /**
         * Return the field alias, can be null.
         * @return Field alias.
         */
        public String getAlias() {
            return alias;
        }

        /**
         * String representation of the field.
         * @return
         */
        @Override
        public String toString() {
            return originalValue;
        }
    }

    public static void main(String[] args) {
        Query query = Query.compile("SeLeCt * FROM holder WHERE (id = 5) OR (name= 8)");
        Query query1 = Query.compile("SELECT * FROM holder WHERE (((id = 5))) OR (name = 'hola') AND id = 8 ");
        Query query2 = Query.compile("SELECT * FROM holder WHERE ((id = 5) OR (name = 8))");
        System.out.println();
    }
}
