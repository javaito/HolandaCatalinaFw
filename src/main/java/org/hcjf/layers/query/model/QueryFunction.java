package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class QueryFunction extends QueryParameter {

    private final String functionName;
    private final List<Object> parameters;

    public QueryFunction(Query query, String originalFunction, String functionName, List<Object> parameters) {
        super(query, originalFunction, functionName);
        this.functionName = functionName;
        this.parameters = parameters;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public Set<QueryResource> getResources() {
        Set<QueryResource> queryResources = new TreeSet<>();

        for(Object parameter : parameters) {
            if(parameter instanceof QueryField) {
                queryResources.add(((QueryField)parameter).getResource());
            } else if(parameter instanceof QueryFunction) {
                queryResources.addAll(((QueryFunction)parameter).getResources());
            }
        }

        return queryResources;
    }

    @Override
    public boolean verifyResource(QueryResource resource) {
        return getResources().contains(resource);
    }
}
