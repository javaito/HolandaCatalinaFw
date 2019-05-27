package org.hcjf.layers.query;

/**
 * @author javaito
 *
 */
public class SmallerThanOrEqual extends SmallerThan {

    public SmallerThanOrEqual(Object leftValue, Object rightValue) {
        super(leftValue, rightValue, true);
    }

}
