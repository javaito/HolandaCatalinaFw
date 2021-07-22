package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;

public class QueryReturnConditional extends QueryParameter implements QueryConditional, QueryReturnParameter {

    private static final String QUERY_PATTERN = "SELECT * FROM resource WHERE %s";

    private final Query evaluationQuery;
    private final String alias;

    public QueryReturnConditional(Query query, String originalValue, String value, String alias) {
        super(query, originalValue, value);
        this.evaluationQuery = Query.compile(String.format(QUERY_PATTERN, originalValue));
        this.alias = alias;
    }

    /**
     * Return query instance that contains all the evaluators.
     * @return Query evaluation instance.
     */
    public Query getEvaluationQuery() {
        return evaluationQuery;
    }

    /**
     * Verify if the query parameter make reference to the specified resource.
     * This implementation always returns false because is only for return parameters.
     * @param resource Resource instance to test.
     * @return Returns true if the parameter make reference to the specified resource and false in the otherwise.
     */
    @Override
    public boolean verifyResource(QueryResource resource) {
        return false;
    }

    /**
     * Return the field alias, can be null.
     *
     * @return Field alias.
     */
    @Override
    public String getAlias() {
        return alias;
    }

}
