package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.properties.SystemProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * @author javaito
 */
public abstract class BaseQueryFunctionLayer extends BaseFunctionLayer implements QueryFunctionLayerInterface {

    private final Set<String> aliases;

    public BaseQueryFunctionLayer(String implName) {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + implName);
        aliases = new HashSet<>();
    }

    /**
     * Adds alias to internal store to deploy the layer with all the
     * aliases added with the expected format name.
     * @param alias Alias name.
     */
    protected final void addFunctionName(String alias) {
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + alias);
    }

    /**
     * Returns the set of the aliases of the layer.
     * @return Aliases of the layer.
     */
    @Override
    public final Set<String> getAliases() {
        return aliases;
    }

    /**
     * Check if the number of parameters and the type are the same that the incoming array.
     * @param functionName Name of the function that are testing.
     * @param parameters Incoming parameters array.
     * @param expectedLength Expected parameters array length.
     * @param types Excepted parameters type.
     */
    protected void checkNumberAndType(String functionName, Object[] parameters, Integer expectedLength, Class... types) {
        if(parameters.length == expectedLength) {
            for (int i = 0; i < parameters.length || i < types.length; i++) {
                if(!types[i].isAssignableFrom(parameters[i].getClass())) {
                    throw new HCJFRuntimeException("The query function '%s' expect something assignable to '%s' class " +
                            "and incoming an instance of '%s' in %d° place");
                }
            }
        } else {
            throw new HCJFRuntimeException("The query function '%s' expect %d number of arguments and incoming %d arguments",
                    functionName, expectedLength, parameters.length);
        }
    }
}
