package org.hcjf.layers.query;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class And extends EvaluatorCollection implements Evaluator {

    public And(EvaluatorCollection parent) {
        super(parent);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer, Object... parameters) {
        boolean result = true;

        for(Evaluator evaluator : getEvaluators()) {
            result &= evaluator.evaluate(object, consumer);
            if(!result) {
                break;
            }
        }

        return result;
    }
}
