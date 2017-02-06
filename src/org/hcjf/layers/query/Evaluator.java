package org.hcjf.layers.query;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public interface Evaluator {

    /**
     * This method must be implemented for each particular implementation
     * to evaluate some details about instances of the data collection.
     * @param object Object of the data collection.
     * @param dataSource Data source
     * @param consumer Consumer
     * @param parameters Evaluation parameters
     * @return Return true if the object must be part of the result add or false in the
     * other ways.
     */
    public boolean evaluate(Object object, Query.DataSource dataSource, Query.Consumer consumer, Object... parameters);
}
