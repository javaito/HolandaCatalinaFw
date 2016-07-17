package org.hcjf.layers.query;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.util.*;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class Query {

    public static final String QUERY_LOG_TAG = "QUERY";

    private final QueryId id;
    private Integer limit;
    private Object pageStart;
    private boolean desc;
    private final List<String> orderFields;
    private final Set<Evaluator> evaluators;

    public Query(QueryId id) {
        this.id = id;
        desc = SystemProperties.getBoolean(SystemProperties.QUERY_DEFAULT_DESC_ORDER);
        limit = SystemProperties.getInteger(SystemProperties.QUERY_DEFAULT_LIMIT);
        orderFields = new ArrayList<>();
        evaluators = new HashSet<>();
    }

    public Query(){
        this(new QueryId());
    }

    public final QueryId getId() {
        return id;
    }

    public final Integer getLimit() {
        return limit;
    }

    public final void setLimit(Integer limit) {
        this.limit = limit;
    }

    public final Object getPageStart() {
        return pageStart;
    }

    public final void setPageStart(Object pageStart) {
        this.pageStart = pageStart;
    }

    public final boolean isDesc() {
        return desc;
    }

    public final void setDesc(boolean desc) {
        this.desc = desc;
    }

    public final Query addOrderField(String orderField) {
        orderFields.add(orderField);
        return this;
    }

    public final Query addEvaluator(Evaluator evaluator) {
        if(evaluator == null) {
            throw new IllegalArgumentException("Null evaluator");
        }

        if(!evaluators.contains(evaluator)) {
            evaluators.add(evaluator);
        } else {
            Log.w(QUERY_LOG_TAG, "Duplicate evaluator: $s", evaluator);
        }
        return this;
    }

    public final Query distinct(String fieldName, Object value) {
        return addEvaluator(new Distinct(fieldName, value));
    }

    public final Query equals(String fieldName, Object value) {
        return addEvaluator(new Equals(fieldName, value));
    }

    public final Query greaterThan(String fieldName, Object value) {
        return addEvaluator(new GreaterThan(fieldName, value));
    }

    public final Query greaterThanOrEquals(String fieldName, Object value) {
        return addEvaluator(new GreaterThanOrEqual(fieldName, value));
    }

    public final Query in(String fieldName, Object value) {
        return addEvaluator(new In(fieldName, value));
    }

    public final Query notIn(String fieldName, Object value) {
        return addEvaluator(new NotIn(fieldName, value));
    }

    public final Query smallerThan(String fieldName, Object value) {
        return addEvaluator(new SmallerThan(fieldName, value));
    }

    public final Query smallerThanOrEqual(String fieldName, Object value) {
        return addEvaluator(new SmallerThanOrEqual(fieldName, value));
    }

    public <O extends Object> Set<O> evaluate(Collection<O> objects) {
        Set<O> result = new TreeSet<>((o1, o2) -> {
            int compareResult = 0;

            if(orderFields.size() > 0) {
                Map<String, Introspection.Getter> getters = Introspection.getGetters(o1.getClass());
                Comparable<Object> comparable1;
                Comparable<Object> comparable2;
                Introspection.Getter getter;
                for(String orderField : orderFields) {
                    getter = getters.get(orderField);
                    if(getter != null) {
                        try {
                            comparable1 = getter.invoke(o1);
                            comparable2 = getter.invoke(o2);
                        } catch (ClassCastException ex) {
                            throw new IllegalArgumentException("Order field must be comparable");
                        } catch (Exception ex) {
                            throw new IllegalArgumentException("Unable to obtain order field value", ex);
                        }
                        compareResult = comparable1.compareTo(comparable2);
                    } else {
                        Log.w(QUERY_LOG_TAG, "Order field not found: %s", orderField);
                    }
                }
            }

            if (compareResult == 0) {
                compareResult = o1.hashCode() - o2.hashCode();
            }

            return compareResult;
        });

        boolean add;
        for(O object : objects) {
            add = true;
            for(Evaluator evaluator : evaluators) {
                add = evaluator.evaluate(object);
                if(!add) {
                    break;
                }
            }
            if(add) {
                result.add(object);
            }
        }

        return result;
    }

    public static final class QueryId {

        private final UUID id;

        private QueryId() {
            this.id = UUID.randomUUID();
        }

        public QueryId(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }
    }
}
