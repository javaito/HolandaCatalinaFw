package org.hcjf.layers.query;

import java.util.Map;

/**
 * This kind of evaluator is used to reduce the query.
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public final class TrueEvaluator implements Evaluator {

    /**
     * Every time return true.
     * @param object Object of the data collection.
     * @param consumer Consumer
     * @return
     */
    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        return true;
    }

}
