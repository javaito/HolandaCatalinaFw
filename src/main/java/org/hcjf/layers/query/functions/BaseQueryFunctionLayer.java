package org.hcjf.layers.query.functions;

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
}
