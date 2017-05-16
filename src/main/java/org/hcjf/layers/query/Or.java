package org.hcjf.layers.query;

import java.util.Map;

/**
 * @author javaito
 *
 */
public class Or extends EvaluatorCollection implements Evaluator {

    public Or(EvaluatorCollection parent) {
        super(parent);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Map<Evaluator, Object> valuesMap) {
        boolean result = false;

        for(Evaluator evaluator : getEvaluators()) {
            result |= evaluator.evaluate(object, consumer, valuesMap);
            if(result) {
                break;
            }
        }

        return result;
    }

}
