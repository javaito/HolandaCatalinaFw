package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.utils.Introspection;

import java.util.Collection;

public class AddAggregateFunction extends BaseQueryAggregateFunctionLayer {

    private static final String NAME = "add";

    public AddAggregateFunction() {
        super(NAME);
    }

    /**
     * Evaluates the specific function.
     * @param alias      Alias of the function
     * @param resultSet  Result set obtained for the query evaluation.
     * @param parameters Function's parameters.
     * @return Function result.
     */
    @Override
    public Collection evaluate(String alias, Collection resultSet, Object... parameters) {
        Collection result = resultSet;
        if(parameters.length == 2) {
            try {
                for(Object row : resultSet) {
                    String path = resolveValue(row, parameters[0]);
                    Object value = resolveValue(row, parameters[1]);
                    if(value instanceof Collection) {
                        Collection collectionValue = (Collection) value;
                        Introspection.resolveAndAdd(row, path, collectionValue);
                    } else {
                        Introspection.resolveAndAdd(row, path, value);
                    }

                }
            } catch (Exception ex){
                throw new HCJFRuntimeException("Min aggregate function fail", ex);
            }
        } else {
            throw new HCJFRuntimeException("Min aggregate function need at leas two parameters");
        }
        return result;
    }
}
