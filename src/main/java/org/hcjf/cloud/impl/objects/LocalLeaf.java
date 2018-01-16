package org.hcjf.cloud.impl.objects;

/**
 * @author javaito
 */
public final class LocalLeaf extends DistributedLeaf {

    private Object instance;

    public LocalLeaf(String name) {
        super(name);
    }

    @Override
    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }
}
