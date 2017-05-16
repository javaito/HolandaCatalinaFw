package org.hcjf.layers.query;

import java.util.Map;

/**
 * @author javaito
 *
 */
public class And extends EvaluatorCollection implements Evaluator {

    public And(EvaluatorCollection parent) {
        super(parent);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        boolean result = true;

        for(Evaluator evaluator : getEvaluators()) {
            result &= evaluator.evaluate(object, consumer, valuesMap);
            if(!result) {
                break;
            }
        }

        return result;
    }
}
