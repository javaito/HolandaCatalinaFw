package org.hcjf.layers.query;

/**
 * @author javaito
 *
 */
public class GreaterThanOrEqual extends GreaterThan {

    public GreaterThanOrEqual(Query.QueryParameter parameter, Object value) {
        super(parameter, value, true);
    }

    public GreaterThanOrEqual(String fieldName, Object value) {
        super(fieldName, value, true);
    }

}
