package org.hcjf.layers.query.model;

import org.hcjf.layers.Layers;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.functions.QueryAggregateFunctionLayerInterface;
import org.hcjf.properties.SystemProperties;

import java.util.List;

public class QueryReturnFunction extends QueryFunction implements QueryReturnParameter {

    private final String alias;
    private boolean aggregate;

    public QueryReturnFunction(Query query, String originalFunction, String functionName, List<Object> parameters, String alias) {
        super(query, originalFunction, functionName, parameters);

        aggregate = false;
        try {
            Layers.get(QueryAggregateFunctionLayerInterface.class,
                    SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) +functionName);
            aggregate = true;
        } catch (Exception ex){}

        if(alias != null) {
            this.alias = alias;
        } else {
            this.alias = originalFunction;
        }
    }

    /**
     * Return the field alias, can be null.
     * @return Field alias.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Verify if the function is an aggregate function or not.
     * @return True if the function is an aggregate function and false in the otherwise.
     */
    public boolean isAggregate() {
        return aggregate;
    }
}
