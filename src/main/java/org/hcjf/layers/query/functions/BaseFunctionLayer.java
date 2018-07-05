package org.hcjf.layers.query.functions;

import org.hcjf.layers.Layer;

/**
 * @author javaito
 */
public abstract class BaseFunctionLayer extends Layer {

    public BaseFunctionLayer(String implName) {
        super(implName);
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
}
