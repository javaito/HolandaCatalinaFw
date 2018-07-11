package org.hcjf.layers.query;

/**
 * @author javaito
 *
 */
public class SmallerThanOrEqual extends SmallerThan {

    public SmallerThanOrEqual(Object leftValue, Object rightValue) {
        super(leftValue, rightValue, true);
    }

    public SmallerThanOrEqual(String fieldName, Object value) {
        super(fieldName, value, true);
    }

}
