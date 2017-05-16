package org.hcjf.layers.storage.actions;

/**
 * @author javaito
 *
 */
public class SingleResult extends ResultSet<Object> {

    public SingleResult(Object result) {
        super(1, result);
    }

}
