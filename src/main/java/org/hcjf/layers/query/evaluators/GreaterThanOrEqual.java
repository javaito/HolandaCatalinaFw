package org.hcjf.layers.query.evaluators;

/**
 * @author javaito
 *
 */
public class GreaterThanOrEqual extends GreaterThan {

    public GreaterThanOrEqual(Object leftValue, Object rightValue) {
        super(leftValue, rightValue, true);
    }

}
