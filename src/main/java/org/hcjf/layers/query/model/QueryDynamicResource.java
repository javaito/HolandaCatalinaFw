package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;

public class QueryDynamicResource extends QueryResource {

    private static final String TO_STRING_PATTERN = "(%s) as %s";

    private final Query query;

    public QueryDynamicResource(String resourceName, Query query) {
        super(resourceName);
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_PATTERN, getQuery().toString(), super.toString());
    }
}
