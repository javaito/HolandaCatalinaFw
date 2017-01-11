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
    private Object pageStart;
    private boolean desc;
    private final List<String> orderFields;
    private final List<String> returnFields;
    private final Map<String, Query> joinQueries;

    public Query(QueryId id) {
        this.id = id;
        desc = SystemProperties.getBoolean(SystemProperties.Query.DEFAULT_DESC_ORDER);
        limit = SystemProperties.getInteger(SystemProperties.Query.DEFAULT_LIMIT);
        orderFields = new ArrayList<>();
        returnFields = new ArrayList<>();
        joinQueries = new HashMap<>();
    }

    public Query(){
        this(new QueryId());
    }

    private Query(Query source) {
        super(source);
        this.id = new QueryId();
        this.resourceName = source.resourceName;
        this.limit = source.limit;
        this.pageStart = source.pageStart;
        this.desc = source.desc;
        this.orderFields = new ArrayList<>();
        this.orderFields.addAll(source.orderFields);
        this.returnFields = new ArrayList<>();
        this.returnFields.addAll(source.returnFields);
        this.joinQueries = new HashMap<>();
        this.joinQueries.putAll(source.joinQueries);
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
     * Return an object that identify the first element of
     * the next page.
     * @return
     */
    public final Object getPageStart() {
        return pageStart;
    }

    /**
     *
     * @param pageStart
     */
    public final void setPageStart(Object pageStart) {
        this.pageStart = pageStart;
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
     * This method evaluate each object of the collection and sort filtered
     * object to create a result add with the object filtered and sorted.
     * If there are order fields added then the result implementation is a
     * {@link TreeSet} implementation else the result implementation is a
     * {@link LinkedHashSet} implementation in order to guarantee the data order
     * from the source
     * @param objects Data collection.
     * @param <O> Kind of instances of the data collection.
     * @return Result add filtered and sorted.
     */
    public final <O extends Object> Set<O> evaluate(Collection<O> objects) {
        Set<O> result;

        if(objects.size() > 0) {
            result = evaluate(objects, new IntrospectionConsumer<>(objects.iterator().next().getClass()));
        } else {
            result = Collections.EMPTY_SET;
        }

        return result;
    }

    /**
     * This method evaluate each object of the collection and sort filtered
     * object to create a result add with the object filtered and sorted.
     * If there are order fields added then the result implementation is a
     * {@link TreeSet} implementation else the result implementation is a
     * {@link LinkedHashSet} implementation in order to guarantee the data order
     * from the source
     * @param objects Data collection.
     * @param consumer Data source consumer.
     * @param <O> Kind of instances of the data collection.
     * @return Result add filtered and sorted.
     */
    public final <O extends Object> Set<O> evaluate(Collection<O> objects, Consumer<O> consumer) {
        Set<O> result;

        //TODO: Aggregation functions


        if(orderFields.size() > 0) {
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
            result = new LinkedHashSet<>();
        }

        boolean add;
        for(O object : objects) {
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

        private final Map<String, Introspection.Getter> getterMap;

        public IntrospectionConsumer(Class clazz) {
            getterMap = Introspection.getGetters(clazz);
        }

        /**
         *
         * @return
         */
        protected final Map<String, Introspection.Getter> getGetterMap() {
            return getterMap;
        }

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
                Introspection.Getter getter = getterMap.get(fieldName);
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

    public static void main(String[] args) {


        System.out.println(Arrays.toString("comowhereasdf".split("((?<=((( innner | left | right )? join )| where | limit | order by | desc ))|(?=((( innner | left | right )? join )| where | limit | order by | desc )))")));

        Query.compile("SELECT * FROM holder WHERE holderid IN (bal) AND (field2 = bla OR field3 = bla)");
    }
}
