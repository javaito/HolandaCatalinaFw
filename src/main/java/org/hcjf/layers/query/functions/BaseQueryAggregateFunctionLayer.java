package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.query.Query;
import org.hcjf.properties.SystemProperties;

/**
 * @author javaito
 */
public abstract class BaseQueryAggregateFunctionLayer extends BaseFunctionLayer implements QueryAggregateFunctionLayerInterface {

    public BaseQueryAggregateFunctionLayer(String implName) {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + implName);
    }

    protected String getPath(Object parameter) {
        String path;
        if(parameter instanceof String) {
            path = (String) parameter;
        } else if(parameter instanceof Query.QueryReturnField) {
            path = ((Query.QueryReturnField)parameter).getFieldPath();
        } else {
            throw new HCJFRuntimeException("Unsupported parameter type");
        }
        return path;
    }
}
