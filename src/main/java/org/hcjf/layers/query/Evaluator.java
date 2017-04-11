package org.hcjf.layers.query;

import java.util.Map;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public interface Evaluator {

    /**
     * This method must be implemented for each particular implementation
     * to evaluate some details about instances of the data collection.
     * @param object Object of the data collection.
     * @param consumer Consumer.
     * @param valuesMap Contains the definitive values to evaluate the query.
     * @return Return true if the object must be part of the result add or false in the
     * other ways.
     */
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap);
}
