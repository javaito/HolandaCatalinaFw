package org.hcjf.cloud.impl.objects;

/**
 * @author javaito
 */
public final class RemoteLeaf extends DistributedLeaf {

    private Object instance;

    public RemoteLeaf(Object key) {
        super(key);
    }

    @Override
    public Object getInstance() {
        return instance != null ? instance : this;
    }

    public final void setInstance(Object instance) {
        this.instance = instance;
    }
}
