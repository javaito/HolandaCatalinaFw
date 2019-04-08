package org.hcjf.layers.query.functions;

import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.ParameterizedQuery;
import org.hcjf.layers.query.Query;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * This function resolve the reference using like parameter one o more UUID
 * and returns a joinable map instance for each reference.
 * @author javaito
 */
public class ReferenceFunctionLayer extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

    private static final String QUERY = "SELECT * FROM %s WHERE id IN (?)";

    public ReferenceFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.REFERENCE_FUNCTION_NAME));
    }

    /**
     * This method resolves the reference of uuid instances and return the joinable map
     * that correspond to the reference object.
     * @param functionName Function name.
     * @param parameters Function's parameters.
     * @return Collection or single instance of joinable map.
     */
    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result;
        String resourceName;
        try{
            resourceName = (String)checkSize(2, parameters)[0];
        } catch (Exception ex){
            throw new IllegalArgumentException("Unrecognized resource name");
        }

        ParameterizedQuery parameterizedQuery = Query.compile(String.format(QUERY, resourceName)).getParameterizedQuery();
        Object param = parameters[1];
        if(param instanceof Collection) {
            parameterizedQuery.add(param);
        } else {
            parameterizedQuery.add(List.of(param));
        }
        result = Query.evaluate(parameterizedQuery);
        return result;
    }
}
