package org.hcjf.layers.query;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.Strings;

import java.util.*;
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
    private String resourceName;
    private Integer limit;
    private Object start;
    private boolean desc;
    private final List<String> orderFields;
    private final List<String> returnFields;
    private final List<Join> joins;

    public Query(QueryId id) {
        this.id = id;
        desc = SystemProperties.getBoolean(SystemProperties.Query.DEFAULT_DESC_ORDER);
        limit = SystemProperties.getInteger(SystemProperties.Query.DEFAULT_LIMIT);
        orderFields = new ArrayList<>();
        returnFields = new ArrayList<>();
        joins = new ArrayList<>();
    }

    public Query(){
        this(new QueryId());
    }

    private Query(Query source) {
        super(source);
        this.id = new QueryId();
        this.resourceName = source.resourceName;
        this.limit = source.limit;
        this.start = source.start;
        this.desc = source.desc;
        this.orderFields = new ArrayList<>();
        this.orderFields.addAll(source.orderFields);
        this.returnFields = new ArrayList<>();
        this.returnFields.addAll(source.returnFields);
        this.joins = new ArrayList<>();
        this.joins.addAll(source.joins);
    }

    /**
     * Return the id of the query.
     * @return Id of the query.
     */
    public final QueryId getId() {
        return id;
    }

    /**
     * Return the resource name.
     * @return Resource name.
     */
    public final String getResourceName() {
        return resourceName;
    }

    /**
     * Set the resource name.
     * @param resourceName Resource name.
     */
    public final void setResourceName(String resourceName) {
        this.resourceName = resourceName;
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
     * Return the way of the sorted method.
     * @return Way of the sorted method. Return true if the the first element
     * is the smaller and the last one is bigger
     */
    public final boolean isDesc() {
        return desc;
    }

    /**
     * Set the way of the sorted method.
     * @param desc Way of the sorted method. Set true for the first element
     * be the smaller and the last be the bigger.
     */
    public final void setDesc(boolean desc) {
        this.desc = desc;
    }

    /**
     * Return the unmodifiable list with order fields.
     * @return Order fields.
     */
    public final List<String> getOrderFields() {
        return Collections.unmodifiableList(orderFields);
    }

    /**
     * Add a name of the field for order the data collection. This name must be exist
     * like a setter/getter method in the instances of the data collection.
     * @param orderField Name of the pair getter/setter.
     * @return Return the same instance of this class.
     */
    public final Query addOrderField(String orderField) {
        orderFields.add(orderField);
        return this;
    }

    /**
     * Return an unmodifiable list with the return fields.
     * @return Return fields.
     */
    public final List<String> getReturnFields() {
        return Collections.unmodifiableList(returnFields);
    }

    /**
     * Add the name of the field to be returned in the result set.
     * @param returnField Field name.
     * @return Return the same instance of this class.
     */
    public final Query addReturnField(String returnField) {
        returnFields.add(returnField);
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
                throw new IllegalArgumentException("Null join instance");
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
    public final <O extends Object> Set<O> evaluate(Collection<O> dataSource) {
        return evaluate((resourceName, evaluators) -> dataSource, new IntrospectionConsumer<>());
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
    public final <O extends Object> Set<O> evaluate(DataSource<O> dataSource) {
        return evaluate(dataSource, new IntrospectionConsumer<>());
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
    public final <O extends Object> Set<O> evaluate(DataSource<O> dataSource, Consumer<O> consumer) {
        Set<O> result;

        //Creating result data collection.
        if(orderFields.size() > 0) {
            //If the query has order fields then creates a tree set with
            //a comparator using the order fields.
            result = new TreeSet<>((o1, o2) -> {
                int compareResult = 0;

                Comparable<Object> comparable1;
                Comparable<Object> comparable2;
                for (String orderField : orderFields) {
                    try {
                        comparable1 = consumer.get(o1, orderField);
                        comparable2 = consumer.get(o2, orderField);
                    } catch (ClassCastException ex) {
                        throw new IllegalArgumentException("Order field must be comparable");
                    }
                    compareResult = comparable1.compareTo(comparable2);
                }

                if (compareResult == 0) {
                    compareResult = o1.hashCode() - o2.hashCode();
                }

                return compareResult * (isDesc() ? -1 : 1);
            });
        } else {
            //If the query has not order fields then creates a linked hash set to
            //manteins the natural order of the data.
            result = new LinkedHashSet<>();
        }

        //Getting data from data dource.
        Collection<O> data;
        if(joins.size() > 0) {
            //If the query has joins then data source must return the joined data
            //collection using all the resources
            data = (Collection<O>) join((DataSource<Joinable>) dataSource, (Consumer<Joinable>) consumer);
        } else {
            //If the query has not joins then data source must return data from
            //resource of the query.
            data = dataSource.getResourceData(getResourceName(), evaluators);
        }

        //Filtering data
        boolean add;
        for(O object : data) {
            add = true;
            for(Evaluator evaluator : getEvaluators()) {
                add = evaluator.evaluate(object, consumer);
                if(!add) {
                    break;
                }
            }
            if(add) {
                result.add(object);
            }
            if(result.size() == getLimit()) {
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

        Collection<Joinable> leftJoinables = dataSource.getResourceData(getResourceName(), evaluators);
        Collection<Joinable> rightJoinables;
        Map<Object, Set<Joinable>> indexedJoineables;
        In in;
        Collection<Evaluator> joinEvaluators = new ArrayList<>();
        for(Join join : joins) {
            indexedJoineables = index(leftJoinables, join.getLeftField(), consumer);
            in = new In(join.getRightField(), indexedJoineables.keySet());
            joinEvaluators.add(in);
            joinEvaluators.addAll(evaluators);
            rightJoinables = dataSource.getResourceData(join.getResourceName(), joinEvaluators);
            leftJoinables.clear();
            for(Joinable rightJoinable : rightJoinables) {
                for(Joinable leftJoinable : indexedJoineables.get(rightJoinable.get(join.getRightField()))) {
                    leftJoinables.add(leftJoinable.join(rightJoinable));
                }
            }
            result.addAll(leftJoinables);
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
                set = new TreeSet<>();
                result.put(key, set);
            }
            set.add(joinable);
        }

        return result;
    }

    /**
     * Return a copy of this query without all the evaluator and order fields of the
     * parameter collections.
     * @param evaluators Evaluators to reduce.
     * @param orderFields Order fields to reduce.
     * @return Reduced copy of the query.
     */
    public final Query reduce(Collection<Evaluator> evaluators, Collection<String> orderFields) {
        Query copy = new Query(this);
        copy.evaluators.addAll(this.evaluators);

        if(evaluators != null && !evaluators.isEmpty()) {
            copy.evaluators.removeAll(evaluators);
        }

        if(orderFields != null && !orderFields.isEmpty()) {
            copy.orderFields.removeAll(orderFields);
        }

        return copy;
    }

    /**
     * Create a query instance from sql definition.
     * @param sql Sql definition.
     * @return Query instance.
     */
    public static Query compile(String sql) {
        Query query = new Query();
        Pattern pattern = SystemProperties.getPattern(SystemProperties.Query.SELECT_REGULAR_EXPRESSION);
        Matcher matcher = pattern.matcher(sql);

        if(matcher.matches()) {
            String selectBody = matcher.group(SystemProperties.getInteger(SystemProperties.Query.SELECT_GROUP_INDEX));
            selectBody = selectBody.replaceFirst(SystemProperties.get(SystemProperties.Query.ReservedWord.SELECT), Strings.EMPTY_STRING);
            String fromBody = matcher.group(SystemProperties.getInteger(SystemProperties.Query.FROM_GROUP_INDEX));
            fromBody = fromBody.replaceFirst(SystemProperties.get(SystemProperties.Query.ReservedWord.FROM), Strings.EMPTY_STRING);
            String conditionalBody = matcher.group(SystemProperties.getInteger(SystemProperties.Query.CONDITIONAL_GROUP_INDEX));

            for(String returnFields : selectBody.split(SystemProperties.get(
                    SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                query.addReturnField(returnFields);
            }

            query.setResourceName(fromBody.trim());

            Pattern conditionalPatter = SystemProperties.getPattern(SystemProperties.Query.CONDITIONAL_REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
            String[] conditionalElements = conditionalPatter.split(conditionalBody);
            String element;
            String elementValue;
            for (int i = 0; i < conditionalElements.length; i++) {
                element = conditionalElements[i++];
                elementValue = conditionalElements[i].trim();
                if(element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.JOIN)) ||
                        element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.INNER_JOIN)) ||
                        element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LEFT_JOIN)) ||
                        element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.RIGHT_JOIN))) {
                    String[] joinElements =  elementValue.split(SystemProperties.get(SystemProperties.Query.JOIN_REGULAR_EXPRESSION));
                    Join join = new Join(
                            joinElements[SystemProperties.getInteger(SystemProperties.Query.JOIN_RESOURCE_NAME_INDEX)],
                            joinElements[SystemProperties.getInteger(SystemProperties.Query.JOIN_RESOURCE_NAME_INDEX)],
                            joinElements[SystemProperties.getInteger(SystemProperties.Query.JOIN_RESOURCE_NAME_INDEX)],
                            Join.JoinType.valueOf(element));
                    query.addJoin(join);
                } else if(element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.WHERE))) {
                    List<String> groups = Strings.replaceableGroup(elementValue);
                    completeWhere(groups, query, groups.size() - 1);
                } else if(element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.ORDER_BY))) {
                    for(String orderField : elementValue.split(SystemProperties.get(
                            SystemProperties.Query.ReservedWord.ARGUMENT_SEPARATOR))) {
                        query.addOrderField(orderField);
                    }
                } else if(element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.DESC))) {
                    query.setDesc(true);
                } else if(element.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.LIMIT))) {
                    query.setLimit(Integer.parseInt(elementValue));
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
    private static final void completeWhere(List<String> groups, EvaluatorCollection parentCollection, int definitionIndex) {
        Pattern wherePatter = SystemProperties.getPattern(SystemProperties.Query.WHERE_REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
        String[] evaluatorDefinitions = wherePatter.split(groups.get(definitionIndex));
        String[] evaluatorValues;
        String fieldValue;
        String operator;
        String value;
        Evaluator evaluator = null;
        EvaluatorCollection collection = null;
        for(String definition : evaluatorDefinitions) {
            definition = definition.trim();
            if (definition.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.AND))) {
                if(collection == null) {
                    collection = parentCollection.and();
                } else if(collection instanceof Or) {
                    collection = collection.and();
                }

                collection.addEvaluator(evaluator);
                evaluator = null;
            } else if (definition.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.OR))) {
                if(collection == null) {
                    collection = parentCollection.or();
                } else if(collection instanceof And) {
                    collection = collection.or();
                }

                collection.addEvaluator(evaluator);
                evaluator = null;
            } else if (definition.startsWith(Strings.REPLACEABLE_GROUP)) {
                Integer index = Integer.parseInt(definition.replace(Strings.REPLACEABLE_GROUP, Strings.EMPTY_STRING));
                completeWhere(groups, collection == null ? parentCollection : collection, index);
            } else {
                evaluatorValues = definition.split(Strings.WHITE_SPACE, 0);
                if (evaluatorValues.length == 3) {
                    fieldValue = evaluatorValues[0].trim();
                    operator = evaluatorValues[1].trim();
                    value = evaluatorValues[2].trim();

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
                        throw new IllegalArgumentException();
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }

        if(evaluator != null) {
            if(collection != null) {
                collection.addEvaluator(evaluator);
            } else {
                parentCollection.addEvaluator(evaluator);
            }
        }
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
                Introspection.Getter getter = Introspection.getGetters(instance.getClass()).get(fieldName);
                if(getter != null) {
                    result = getter.get(instance);
                } else {
                    Log.w(SystemProperties.get(SystemProperties.Query.LOG_TAG),
                            "Order field not found: %s", fieldName);
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
         * @param resourceName Name of the resource.
         * @param evaluators List with the evaluators to filter the resource data.
         * @return Data collection from the resource.
         */
        public Collection<O> getResourceData(String resourceName, Collection<Evaluator> evaluators);

    }
}
