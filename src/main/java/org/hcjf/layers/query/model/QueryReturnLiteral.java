package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;

public class QueryReturnLiteral extends QueryParameter implements QueryReturnParameter {

    private final String alias;
    private final Object value;

    public QueryReturnLiteral(Query query, String originalValue, String valueToString, Object value, String alias) {
        super(query, originalValue, valueToString);
        this.alias = alias;
        this.value = value;
    }

    /**
     * Returns the literal value.
     * @return Literal value.
     */
    public Object getValue() {
        return value;
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
     * Verify if the query parameter make reference to the specified resource.
     * @param resource Resource instance to test.
     * @return Returns true if the parameter make reference to the specified resource and false in the otherwise.
     */
    @Override
    public boolean verifyResource(QueryResource resource) {
        return false;
    }
}
