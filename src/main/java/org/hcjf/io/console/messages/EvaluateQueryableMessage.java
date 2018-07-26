package org.hcjf.io.console.messages;

import org.hcjf.io.net.messages.Message;
import org.hcjf.layers.query.Queryable;

/**
 * This message contains a queryable to execute.
 * @author javaito
 */
public class EvaluateQueryableMessage extends Message {

    private Queryable queryable;

    /**
     * Returns the queryable instance.
     * @return Queryable.
     */
    public Queryable getQueryable() {
        return queryable;
    }

    /**
     * Set the queryable instance.
     * @param queryable Queryable instance.
     */
    public void setQueryable(Queryable queryable) {
        this.queryable = queryable;
    }
}
