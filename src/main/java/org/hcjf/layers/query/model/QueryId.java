package org.hcjf.layers.query.model;

import java.util.UUID;

/**
 * Represents an query id. Wrapper of the UUID class.
 */
public final class QueryId {

    private final UUID id;

    public QueryId() {
        this.id = UUID.randomUUID();
    }

    public QueryId(UUID id) {
        this.id = id;
    }

    /**
     * Get the UUID instance.
     * @return UUID instance.
     */
    public UUID getId() {
        return id;
    }
}
