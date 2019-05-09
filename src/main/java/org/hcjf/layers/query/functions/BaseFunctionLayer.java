package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layer;

/**
 * @author javaito
 */
public abstract class BaseFunctionLayer extends Layer {

    public BaseFunctionLayer(String implName) {
        super(implName);
    }

    /**
     * This utils method returns a specific parameter and cast it with the expected type in the invoker.
     * @param index Argument index.
     * @param parameters Arguments array.
     * @param <O> Expected data type for the invoker.
     * @return Parameter value.
     */
    protected <O extends Object> O getParameter(int index, Object... parameters) {
        try {
            return (O) parameters[index];
        } catch (ClassCastException ex) {
            throw new HCJFRuntimeException("Illegal argument type, %d° argument", ex, index);
        } catch (IndexOutOfBoundsException ex) {
            throw new HCJFRuntimeException("Wrong number of arguments, getting %d° argument", ex, index);
        } catch (Exception ex) {
            throw new HCJFRuntimeException("Unexpected error getting %d° argument", ex, index);
        }
    }
}
