package org.hcjf.layers.storage.actions;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class SingleResult extends ResultSet<Object> {

    public SingleResult(Object result) {
        super(1, result);
    }

}
