package org.hcjf.layers.query.compilers;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layer;
import org.hcjf.layers.query.Join;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.evaluators.Evaluator;
import org.hcjf.layers.query.model.*;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class JsonCompiler extends Layer implements QueryCompiler {

    private static final String NAME = "json";

    public static final class Fields {
        public static final String ENVIRONMENT = "environment";
        public static final String FIELDS = "fields";
        public static final String FROM = "from";
        public static final String WHERE = "where";
        public static final String JOIN = "join";
        public static final String GROUP_BY = "groupBy";
        public static final String DISJOINT = "disjoint";
        public static final String ORDER_BY = "orderBy";
        public static final String START = "start";
        public static final String UNDERLYING_START = "underlyingStart";
        public static final String LIMIT = "limit";
        public static final String UNDERLYING_LIMIT = "underlyingLimit";
        public static final String AS = "as";
        public static final String QUERY = "query";
        public static final String DATA = "data";
        public static final String PATH = "path";
        public static final String FUNCTION = "function";
        public static final String PARAMETERS = "parameters";
    }

    @Override
    public String getImplName() {
        return NAME;
    }

    /**
     * Create a query instance from a expression.
     * @param queryExpression Expression that represents a query.
     * @return Query instance.
     */
    @Override
    public Query compile(String queryExpression) {
        Map<String,Object> queryModel = (Map<String, Object>) JsonUtils.createObject(queryExpression);
        return compile(queryModel);
    }

    /**
     * Create a query instance from a query model.
     * @param queryModel Query model.
     * @return Query instance.
     */
    private Query compile(Map<String,Object> queryModel) {
        Object from = Introspection.resolve(queryModel, Fields.FROM);
        Query query = new Query(createResource(from));

        //Environment
        query.setEnvironment(Introspection.resolve(queryModel, Fields.ENVIRONMENT));

        //Fields
        Collection<Object> fields = Introspection.resolve(queryModel, Fields.FIELDS);
        if(fields != null) {
            for (Object fieldBody : fields) {
                query.addReturnField(createQueryReturnParameter(query, fieldBody));
            }
        }

        //Evaluators
        Collection<Object> evaluators = Introspection.resolve(queryModel, Fields.WHERE);
        if(evaluators != null) {
            for (Object evaluatorBody : evaluators) {
                query.addEvaluator(createEvaluator(query, evaluatorBody));
            }
        }

        //Joins
        Collection<Object> joins = Introspection.resolve(queryModel, Fields.JOIN);
        if(joins != null) {
            for (Object joinModel : joins) {
                query.addJoin(createJoin(query, joinModel));
            }
        }

        //Group By / Disjoint
        Collection<Object> groupFields = null;
        if(queryModel.containsKey(Fields.GROUP_BY)) {
            groupFields = Introspection.resolve(queryModel, Fields.FIELDS);
        } else if(queryModel.containsKey(Fields.DISJOINT)) {
            groupFields = Introspection.resolve(queryModel, Fields.DISJOINT);
            query.setDisjoint(true);
        }
        if(groupFields != null) {
            for (Object groupBody : groupFields) {
                query.addGroupField(createQueryReturnParameter(query, groupBody));
            }
        }

        //Order
        Collection<Object> orderParameters = Introspection.resolve(queryModel, Fields.ORDER_BY);
        if(orderParameters != null) {
            for(Object order : orderParameters) {
                query.addOrderParameter(createQueryOrderParameter(query, order));
            }
        }

        if(queryModel.containsKey(Fields.START)) {
            query.setStart(Introspection.resolve(queryModel, Fields.START));
        }
        if(queryModel.containsKey(Fields.UNDERLYING_START)) {
            query.setUnderlyingStart(Introspection.resolve(queryModel, Fields.UNDERLYING_START));
        }
        if(queryModel.containsKey(Fields.LIMIT)) {
            query.setLimit(Introspection.resolve(queryModel, Fields.LIMIT));
        }
        if(queryModel.containsKey(Fields.UNDERLYING_LIMIT)) {
            query.setUnderlyingLimit(Introspection.resolve(queryModel, Fields.UNDERLYING_LIMIT));
        }

        return query;
    }

    /**
     * Evaluates the class of object and generate the resource instance.
     * If the object instance of string, the the resource is a named query resource.
     * If the object instance of a map and contains 'query' field then the resource instance is a dynamic resource.
     * If the object instance of a map and contains 'data' field then the resource instance is a json resource.
     * @param from From body.
     * @return Resource instance.
     */
    private QueryResource createResource(Object from) {
        QueryResource result;
        if(from != null) {
            if (from instanceof String) {
                result = new QueryResource((String)from);
            } else if (from instanceof Map) {
                Map<String,Object> body = (Map<String, Object>) from;
                String alias = Introspection.resolve(body, Fields.AS);
                if(alias == null || alias.isBlank()) {
                    throw new HCJFRuntimeException("For complex resource the alias is required");
                }
                if(body.containsKey(Fields.QUERY)) {
                    Map<String,Object> subQueryModel = Introspection.resolve(Fields.QUERY);
                    String path = Introspection.resolve(Fields.PATH);
                    Query subQuery = compile(subQueryModel);
                    result = new QueryDynamicResource(alias, subQuery, path);
                } else if(body.containsKey(Fields.DATA)) {
                    Object data = Introspection.resolve(Fields.DATA);
                    Collection<Map<String,Object>> collection;
                    if(data instanceof Map) {
                        collection = new ArrayList<>();
                        collection.add((Map<String, Object>) data);
                    } else if(data instanceof Collection) {
                        collection = (Collection<Map<String, Object>>) data;
                    } else {
                        throw new HCJFRuntimeException("");
                    }
                    result = new QueryJsonResource(alias, collection);
                } else {
                    throw new HCJFRuntimeException("");
                }
            } else {
                throw new HCJFRuntimeException("Incompatible resource type %s expected resource name or resource object");
            }
        } else {
            throw new HCJFRuntimeException("The 'from' is not present into query model.");
        }
        return result;
    }

    /**
     *
     * @param query
     * @param fieldBody
     * @return
     */
    private QueryReturnParameter createQueryReturnParameter(Query query, Object fieldBody) {
        QueryReturnParameter result = null;

        if(fieldBody instanceof Map) {

        }

        return result;
    }

    /**
     *
     * @param query
     * @param evaluatorBody
     * @return
     */
    private Evaluator createEvaluator(Query query, Object evaluatorBody) {
        return null;
    }

    /**
     *
     * @param query
     * @param joinBody
     * @return
     */
    private Join createJoin(Query query, Object joinBody) {
        return null;
    }

    /**
     *
     * @param query
     * @param orderBody
     * @return
     */
    private QueryOrderParameter createQueryOrderParameter(Query query, Object orderBody) {
        return null;
    }
}
