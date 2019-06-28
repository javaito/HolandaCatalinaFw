package org.hcjf.layers.distributed;

import org.hcjf.cloud.Cloud;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class is an interceptor component to make the distributed call.
 * @author javaito
 */
public final class DistributedLayer extends Layer {

    private final Class<? extends LayerInterface> layerClass;

    public DistributedLayer(String implName, Class<? extends LayerInterface> layerClass) {
        super(implName);
        this.layerClass = layerClass;
    }

    public Object invoke(Object proxy, Method method, Object[] args) {
        Object result;
        if(method.getDeclaringClass().equals(Object.class) || method.getDeclaringClass().equals(LayerInterface.class)) {
            try {
                result = method.invoke(this, args);
            } catch (Exception ex) {
                throw new HCJFRuntimeException("Distributed layer invoke fail", ex);
            }
        } else {
            result = Cloud.layerInvoke(layerClass, getImplName(), method, args);
        }
        return result;
    }

}
