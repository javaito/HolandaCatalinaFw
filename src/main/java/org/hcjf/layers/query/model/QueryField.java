package org.hcjf.layers.query.model;

import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;
import org.hcjf.utils.Introspection;

/**
 * This class represents any kind of query fields.
 */
public class QueryField extends QueryParameter {

    public QueryField(Query query, String fieldPath) {
        super(query, fieldPath, fieldPath);
    }

    /**
     * This method resolves the introspection over the instance using the information into the
     * query field.
     * @param instance Instance to resolve the introspection.
     * @param <R> Expected data type.
     * @return Introspection result.
     */
    public <R extends Object> R resolve(Object instance) {
        Object result = null;
        if(instance instanceof JoinableMap && ((JoinableMap) instance).getResources().size() > 1) {
            if(getResource().equals(QueryResource.ANY)) {
                for(String resourceName : ((JoinableMap)instance).getResources()) {
                    result = Introspection.silentResolve(((JoinableMap) instance).
                            getResourceModel(resourceName), getFieldPath());
                    if(result != null) {
                        break;
                    }
                }
                if(result == null) {
                    result = Introspection.silentResolve(instance, getFieldPath());
                }
            } else {
                result = Introspection.silentResolve(((JoinableMap) instance).
                        getResourceModel(getResource().getResourceName()), getFieldPath());
            }
        } else {
            result = Introspection.silentResolve(instance, getFieldPath());
        }
        return (R) result;
    }

    @Override
    public boolean verifyResource(QueryResource resource) {
        return getResource().equals(resource);
    }
}
