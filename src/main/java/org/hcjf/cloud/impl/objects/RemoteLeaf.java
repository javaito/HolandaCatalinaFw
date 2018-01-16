package org.hcjf.cloud.impl.objects;

/**
 * @author javaito
 */
public final class RemoteLeaf extends DistributedLeaf {

    public RemoteLeaf(String name) {
        super(name);
    }

    @Override
    public Object getInstance() {
        return null;
    }
}
