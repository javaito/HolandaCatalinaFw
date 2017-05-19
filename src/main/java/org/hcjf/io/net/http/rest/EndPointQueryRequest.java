package org.hcjf.io.net.http.rest;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.layers.query.Query;

/**
 * @author javaito
 */
public class EndPointQueryRequest extends EndPointRequest {

    private final Query query;

    public EndPointQueryRequest(HttpRequest request, Query query) {
        super(request);
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }
}
