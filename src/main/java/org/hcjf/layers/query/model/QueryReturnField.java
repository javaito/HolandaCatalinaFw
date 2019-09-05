package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;

/**
 * This kind of component represent the fields to be returned into the query.
 */
public class QueryReturnField extends QueryField implements QueryReturnParameter {

    private final String alias;

    public QueryReturnField(Query query, String fieldPath) {
        this(query, fieldPath, fieldPath);
    }

    public QueryReturnField(Query query, String fieldPath, String alias) {
        super(query, fieldPath);
        if(alias == null) {
            this.alias = fieldPath;
        } else {
            this.alias = alias;
        }
    }

    /**
     * Return the field alias, can be null.
     * @return Field alias.
     */
    public String getAlias() {
        return alias;
    }

}
