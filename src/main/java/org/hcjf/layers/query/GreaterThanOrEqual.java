package org.hcjf.layers.query;

/**
 * @author javaito
 *
 */
public class GreaterThanOrEqual extends GreaterThan {

    public GreaterThanOrEqual(Object leftValue, Object rightValue) {
        super(leftValue, rightValue, true);
    }

    public GreaterThanOrEqual(String fieldName, Object value) {
        super(fieldName, value, true);
    }

}
