package org.hcjf.layers.query.functions;

import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

/**
 * @author javaito
 */
public abstract class BaseQueryAggregateFunctionLayer extends BaseFunctionLayer implements QueryAggregateFunctionLayerInterface {

    public BaseQueryAggregateFunctionLayer(String implName) {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + implName);
    }

    protected Object resolve(Object instance, Query.QueryReturnField queryReturnField) {
        Object result;
        if(instance instanceof JoinableMap && ((JoinableMap)instance).containsResource(queryReturnField.getResource().getResourceName())) {
            result = Introspection.resolve(((JoinableMap)instance).getResourceModel(queryReturnField.getResource().getResourceName()), queryReturnField.getFieldPath());
        } else {
            result = Introspection.resolve(instance, queryReturnField.getFieldPath());
        }
        return result;
    }
}
