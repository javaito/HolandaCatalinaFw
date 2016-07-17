package org.hcjf.layers.query;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class GreaterThanOrEqual extends Evaluator {

    public GreaterThanOrEqual(String fieldName, Object value) {
        super(fieldName, value);
    }

    @Override
    protected boolean evaluate(Object object) {
        return false;
    }
}
