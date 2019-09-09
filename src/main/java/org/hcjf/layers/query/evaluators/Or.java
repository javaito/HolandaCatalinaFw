package org.hcjf.layers.query.evaluators;

import org.hcjf.layers.query.Queryable;
import org.hcjf.layers.query.model.QueryParameter;

/**
 * Evaluate all the evaluators into the collection and concat all the result with or operation.
 * @author javaito
 */
public class Or extends EvaluatorCollection implements Evaluator {

    public Or(EvaluatorCollection parent) {
        super(parent);
    }

    /**
     * Evaluate all the inner evaluators.
     * @param object Object of the data collection.
     * @param dataSource Data source.
     * @param consumer Consumer.
     * @return Returns the value of the concat all the values.
     */
    @Override
    public boolean evaluate(Object object, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        boolean result = false;

        for(Evaluator evaluator : getEvaluators()) {
            if (evaluator instanceof BooleanEvaluator &&
                    ((BooleanEvaluator) evaluator).getValue() instanceof QueryParameter &&
                    ((QueryParameter)((BooleanEvaluator) evaluator).getValue()).isUnderlying()) {
                result = true;
            } else {
                result |= evaluator.evaluate(object, dataSource, consumer);
            }
            if(result) {
                break;
            }
        }

        return result;
    }

}
