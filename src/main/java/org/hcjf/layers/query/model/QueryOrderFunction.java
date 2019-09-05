package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;

import java.util.List;

public class QueryOrderFunction extends QueryFunction implements QueryOrderParameter {

    private final boolean desc;

    public QueryOrderFunction(Query query, String originalFunction, String functionName, List<Object> parameters, boolean desc) {
        super(query, originalFunction, functionName, parameters);
        this.desc = desc;
    }

    /**
     * Return the desc property.
     * @return Desc property.
     */
    public boolean isDesc() {
        return desc;
    }

}
