package org.hcjf.layers.distributed;

import org.hcjf.cloud.Cloud;
import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;

import java.lang.reflect.Method;

/**
 * @author javaito
 */
public final class DistributedLayer extends Layer {

    private final Class<? extends LayerInterface> layerClass;

    public DistributedLayer(String implName, Class<? extends LayerInterface> layerClass) {
        super(implName);
        this.layerClass = layerClass;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return Cloud.layerInvoke(layerClass, getImplName(), method, args);
    }

}
