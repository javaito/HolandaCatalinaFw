package org.hcjf.layers.query;

/**
 * @author javaito
 *
 */
public class SmallerThanOrEqual extends SmallerThan {

    public SmallerThanOrEqual(Query.QueryParameter parameter, Object value) {
        super(parameter, value, true);
    }

    public SmallerThanOrEqual(String fieldName, Object value) {
        super(fieldName, value, true);
    }

}
