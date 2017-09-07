package org.hcjf.layers.query.functions;

import org.hcjf.layers.query.JoinableMap;
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
        if(checkSize(1, parameters)[0] instanceof UUID) {
            UUID uuid = (UUID) parameters[0];
            result = new JoinableMap(Introspection.toMap(Query.evaluate(uuid)));
        } else if(parameters[0] instanceof List) {
            Collection<JoinableMap> collection = new ArrayList<>();
            List<UUID> ids = (List<UUID>) parameters[0];
            for(UUID uuid : ids) {
                collection.add(new JoinableMap(Introspection.toMap(Query.evaluate(uuid))));
            }
            result = collection;
        } else {
            throw new IllegalArgumentException("Reference function supports only uuid or list of uuid as parameter");
        }
        return result;
    }
}
