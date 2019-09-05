package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;

/**
 * This class represents a order field with desc property
 */
public class QueryOrderField extends QueryField implements QueryOrderParameter {

    private final boolean desc;

    public QueryOrderField(Query query, String fieldPath, boolean desc) {
        super(query, fieldPath);
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
