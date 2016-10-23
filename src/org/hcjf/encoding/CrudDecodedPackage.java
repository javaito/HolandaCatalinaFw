package org.hcjf.encoding;

import org.hcjf.layers.query.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class CrudDecodedPackage extends DecodedPackage {

    private final Query query;

    public CrudDecodedPackage(Object object, Query query, Map<String, Object> parameters) {
        super(object, parameters);
        this.query = query;
    }

    /**
     * Return the query object decoded.
     * @return Object query or null.
     */
    public final Query getQuery() {
        return query;
    }

}
