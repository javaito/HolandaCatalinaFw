package org.hcjf.cloud.impl.objects;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.List;
import java.util.UUID;

/**
 * @author javaito
 */
public final class RemoteLeaf extends DistributedLeaf {

    private RemoteValue remoteValue;

    public RemoteLeaf(Object key) {
        super(key);
    }

    public RemoteValue getRemoteValue() {
        return remoteValue;
    }

    public void setRemoteValue(RemoteValue remoteValue) {
        this.remoteValue = remoteValue;
    }

    @Override
    public Object getInstance() {
        return remoteValue;
    }

    public static final class RemoteValue implements BsonParcelable {

        private List<UUID> nodes;

        public RemoteValue() {
        }

        public List<UUID> getNodes() {
            return nodes;
        }

        public void setNodes(List<UUID> nodes) {
            this.nodes = nodes;
        }
    }
}
