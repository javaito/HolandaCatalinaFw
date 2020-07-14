package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.evaluators.BaseEvaluator;

public class QueryReturnUnprocessedValue extends QueryParameter implements QueryReturnParameter {

    private final String alias;
    private final BaseEvaluator.UnprocessedValue unprocessedValue;

    public QueryReturnUnprocessedValue(Query query, String originalValue, String alias, BaseEvaluator.UnprocessedValue unprocessedValue) {
        super(query, originalValue, "resultSet");
        this.alias = alias;
        this.unprocessedValue = unprocessedValue;
    }

    /**
     * Return the field alias, can be null.
     * @return Field alias.
     */
    @Override
    public String getAlias() {
        return alias;
    }

    /**
     * Returns the unprocessed value instance.
     * @return Unprocessed value instance.
     */
    public final BaseEvaluator.UnprocessedValue getUnprocessedValue() {
        return unprocessedValue;
    }

    /**
     * Verify if the component is underlying.
     * @return True if the component is underlying.
     */
    @Override
    public boolean isUnderlying() {
        return false;
    }

    /**
     * Verify if the query parameter make reference to the specified resource.
     *
     * @param resource Resource instance to test.
     * @return Returns true if the parameter make reference to the specified resource and false in the otherwise.
     */
    @Override
    public boolean verifyResource(QueryResource resource) {
        return true;
    }

}
