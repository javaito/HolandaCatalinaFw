package org.hcjf.layers.query;

/**
 * @author javaito
 *
 */
public interface Evaluator {

    /**
     * This method must be implemented for each particular implementation
     * to evaluate some details about instances of the data collection.
     * @param object Object of the data collection.
     * @param dataSource Data source.
     * @param consumer Consumer.
     * @return Return true if the object must be part of the result add or false in the
     * other ways.
     */
    boolean evaluate(Object object, Queryable.DataSource dataSource, Queryable.Consumer consumer);
}
