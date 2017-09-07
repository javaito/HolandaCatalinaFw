package org.hcjf.layers.query.functions;

import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * @author javaito
 */
public abstract class BaseQueryFunctionLayer extends Layer implements QueryFunctionLayerInterface {

    private final Set<String> aliases;

    public BaseQueryFunctionLayer(String implName) {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + implName);
        aliases = new HashSet<>();
    }

    /**
     * Check the number of parameter before call the specific function.
     * @param size Parameters size to check.
     * @param parameters Original array of parameters.
     * @return Return the same original array of parameters.
     * @throws IllegalArgumentException if the size to check is not equals to the length of original parameters array.
     */
    protected Object[] checkSize(int size, Object... parameters) {
        if(parameters.length != size) {
            throw new IllegalArgumentException("Illegal parameters length");
        }
        return parameters;
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
}
