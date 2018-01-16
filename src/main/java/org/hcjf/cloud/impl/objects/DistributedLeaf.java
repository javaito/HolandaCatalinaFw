package org.hcjf.cloud.impl.objects;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author javaito
 */
public abstract class DistributedLeaf implements DistributedObject {

    private final String name;
    private final Set<UUID> nodes;
    private Long lastUpdate;

    public DistributedLeaf(String name) {
        this.name = name;
        this.nodes = new HashSet<>();
    }

    @Override
    public String getName() {
        return name;
    }

    public final Long getLastUpdate() {
        return lastUpdate;
    }

    public final void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
