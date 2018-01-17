package org.hcjf.cloud.impl.objects;

/**
 * @author javaito
 */
public final class LocalLeaf extends DistributedLeaf {

    private Object instance;

    public LocalLeaf(Object key) {
        super(key);
    }

    @Override
    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }
}
