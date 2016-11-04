package org.hcjf.layers.query;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class Or extends EvaluatorCollection implements Evaluator {

    public Or(EvaluatorCollection parent) {
        super(parent);
    }

    @Override
    public boolean evaluate(Object object, Query.Consumer consumer) {
        boolean result = false;

        for(Evaluator evaluator : getEvaluators()) {
            result |= evaluator.evaluate(object, consumer);
            if(result) {
                break;
            }
        }

        return result;
    }

}
