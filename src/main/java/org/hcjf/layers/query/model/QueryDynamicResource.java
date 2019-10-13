package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;

public class QueryDynamicResource extends QueryResource {

    private static final String TO_STRING_PATTERN = "(%s) as %s";
    private static final String TO_STRING_WITH_PATH_PATTERN = "(%s).%s as %s";

    private final Query query;
    private final String path;

    public QueryDynamicResource(String resourceName, Query query) {
        this(resourceName, query, null);
    }

    public QueryDynamicResource(String resourceName, Query query, String path) {
        super(resourceName);
        this.query = query;
        this.path = path;
    }

    public Query getQuery() {
        return query;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return  path == null ?
                String.format(TO_STRING_PATTERN, getQuery().toString(), super.toString()) :
                String.format(TO_STRING_WITH_PATH_PATTERN, getQuery().toString(), getPath(), super.toString());
    }
}
