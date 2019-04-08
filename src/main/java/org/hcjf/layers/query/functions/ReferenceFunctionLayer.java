package org.hcjf.layers.query.functions;

import org.hcjf.layers.query.ParameterizedQuery;
import org.hcjf.layers.query.Query;
import org.hcjf.properties.SystemProperties;

import java.util.Collection;
import java.util.List;

/**
 * This function resolve the reference using like parameter one o more UUID
 * and returns a joinable map instance for each reference.
 * @author javaito
 */
public class ReferenceFunctionLayer extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

    private static final String QUERY = "SELECT * FROM %s WHERE %s IN ?";

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
        String referenceField = "id";
        Object param;
        try{
            resourceName = (String)checkSize(3, parameters)[0];
        } catch (Exception ex){
            throw new IllegalArgumentException("Unrecognized resource name");
        }

        if(parameters.length >= 3) {
            try {
                referenceField = (String) parameters[1];
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unrecognized reference field");
            }
            param = parameters[2];
        } else {
            param = parameters[1];
        }

        ParameterizedQuery parameterizedQuery = Query.compile(String.format(QUERY, resourceName, referenceField)).getParameterizedQuery();
        if(param != null) {
            if (param instanceof Collection) {
                parameterizedQuery.add(param);
                result = Query.evaluate(parameterizedQuery);
            } else {
                parameterizedQuery.add(List.of(param));
                Collection collection = Query.evaluate(parameterizedQuery);
                if(collection.size() == 1) {
                    result = collection.stream().findFirst().get();
                } else {
                    result = null;
                }
            }
        } else {
            result = null;
        }
        return result;
    }
}
