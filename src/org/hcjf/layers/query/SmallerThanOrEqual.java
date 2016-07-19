package org.hcjf.layers.query;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class SmallerThanOrEqual extends SmallerThan {

    public SmallerThanOrEqual(String fieldName, Object value) {
        super(fieldName, value, true);
    }

}
