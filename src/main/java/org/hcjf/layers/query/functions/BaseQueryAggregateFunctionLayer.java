package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;

/**
 * @author javaito
 */
public abstract class BaseQueryAggregateFunctionLayer extends BaseFunctionLayer implements QueryAggregateFunctionLayerInterface {

    public BaseQueryAggregateFunctionLayer(String implName) {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + implName);
    }

}
