package org.hcjf.layers.query.evaluators;

import org.hcjf.layers.query.Queryable;

/**
 * @author javaito
 *
 */
public class NotIn extends In {

    public NotIn(Object leftValue, Object rightValue) {
        super(leftValue, rightValue);
    }

    @Override
    public boolean evaluate(Object object, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        return !super.evaluate(object, dataSource, consumer);
    }
}
