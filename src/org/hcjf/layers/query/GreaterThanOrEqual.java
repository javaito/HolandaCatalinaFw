package org.hcjf.layers.query;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class GreaterThanOrEqual extends GreaterThan {

    public GreaterThanOrEqual(String fieldName, Object value) {
        super(fieldName, value, true);
    }

}
