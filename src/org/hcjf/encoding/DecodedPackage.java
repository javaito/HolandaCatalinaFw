package org.hcjf.encoding;

import org.hcjf.layers.query.Query;

import java.util.Map;

/**
 * The instances of this class are the resulting of decode process.
 * @author javaito
 * @mail javaito@gmail.com
 */
public class DecodedPackage {

    private final Object object;
    private final Query query;
    private final Map<String, Object> parameters;

    public DecodedPackage(Object object, Query query, Map<String, Object> parameters) {
        if(parameters == null) {
            throw new IllegalArgumentException("Parameters map can't be null");
        }
        this.object = object;
        this.query = query;
        this.parameters = parameters;
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
