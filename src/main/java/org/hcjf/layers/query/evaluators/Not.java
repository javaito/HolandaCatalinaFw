package org.hcjf.layers.query.evaluators;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.query.Queryable;

public class Not extends FieldEvaluator {

    public Not(Object leftValue) {
        super(leftValue, null);
    }

    /**
     * This method must be implemented for each particular implementation
     * to evaluate some details about instances of the data collection.
     *
     * @param object     Object of the data collection.
     * @param dataSource Data source.
     * @param consumer   Consumer.
     * @return Return true if the object must be part of the result add or false in the
     * other ways.
     */
    @Override
    public boolean evaluate(Object object, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        boolean result;
        Object leftValue = getProcessedLeftValue(object, dataSource, consumer);

        if(leftValue instanceof Boolean) {
            result = !(Boolean) leftValue;
        } else {
            throw new HCJFRuntimeException("Not evaluator expecting a boolean expression");
        }

        return result;
    }
}
