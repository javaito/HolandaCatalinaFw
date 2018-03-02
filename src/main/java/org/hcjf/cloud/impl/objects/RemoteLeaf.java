package org.hcjf.cloud.impl.objects;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author javaito
 */
public final class RemoteLeaf extends DistributedLeaf {


    public RemoteLeaf(Object key) {
        super(key);
    }

    @Override
    public Object getInstance() {
        return this;
    }

}
