package org.hcjf.layers.business;

import org.hcjf.layers.Layer;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class BusinessLayer extends Layer {

    public BusinessLayer(String implName, boolean stateful) {
        super(implName, stateful);
    }

    public BusinessLayer(String implName) {
        super(implName);
    }

}
