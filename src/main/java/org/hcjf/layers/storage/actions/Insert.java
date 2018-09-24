package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Insert storage operation.
 * @author javaito
 */
public abstract class Insert<S extends StorageSession, A extends Object> extends ManipulationDataAction<S, A> {

    private Object instance;

    public Insert(S session, Class modelClass, Object instance) {
        super(session, modelClass);
        Objects.requireNonNull(instance, "Unsupported null instances");
        this.instance = instance;
    }

    public Insert(S session, Object instance) {
        this(session, Map.class, instance);
    }

    /**
     * Returns only one instance to insert.
     * @return Instance to insert.
     */
    @Override
    protected final Collection<Object> getInstances() {
        return List.of(instance);
    }

}
