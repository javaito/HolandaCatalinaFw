package org.hcjf.layers.query.functions;

import org.hcjf.layers.query.Queryable;
import org.hcjf.layers.query.model.QueryReturnConditional;
import org.hcjf.layers.query.model.QueryReturnField;
import org.hcjf.layers.query.model.QueryReturnFunction;
import org.hcjf.properties.SystemProperties;

/**
 * @author javaito
 */
public abstract class BaseQueryAggregateFunctionLayer extends BaseFunctionLayer implements QueryAggregateFunctionLayerInterface {

    public BaseQueryAggregateFunctionLayer(String implName) {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + implName);
    }

    protected <O extends Object> O resolveValue(Object row, Object value) {
        Object result = value;
        if(result instanceof QueryReturnField) {
            result = ((QueryReturnField)result).resolve(row);
        } else if(result instanceof QueryReturnFunction) {
            result = new Queryable.IntrospectionConsumer<>().get(row, ((QueryReturnFunction)result), null);
        } else if(result instanceof QueryReturnConditional) {
            result = ((QueryReturnConditional)result).getEvaluationQuery().verifyCondition(row);
        }
        return (O) result;
    }
}
