package org.hcjf.encoding;

import org.hcjf.errors.Errors;
import org.hcjf.layers.query.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class CrudDecodedPackage extends DecodedPackage {

    private final Object object;
    private final Map<String, Object> parameters;
    private final Query query;

    public CrudDecodedPackage(Object object, Query query, Map<String, Object> parameters) {
        if(parameters == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_ENCODING_1));
        }
        this.object = object;
        this.parameters = parameters;
        this.query = query;
    }

    /**
     * Return the query object decoded.
     * @return Object query or null.
     */
    public final Query getQuery() {
        return query;
    }

    /**
     * Return the body object decoded
     * @return Object query or null.
     */
    public final Object getObject() {
        return object;
    }

    /**
     * Return the map of parameters
     * @return Map of the parameters.
     */
    public final Map<String, Object> getParameters() {
        return parameters;
    }

}
