package org.hcjf.layers;

import java.lang.reflect.InvocationHandler;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public interface LayerInterface extends InvocationHandler {

    /**
     *
     * @return
     */
    public String getImplName();

    /**
     *
     * @return
     */
    public boolean isStateful();

}
