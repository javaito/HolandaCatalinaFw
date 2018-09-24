package org.hcjf.layers.storage.actions;

import org.hcjf.layers.query.Queryable;
import org.hcjf.layers.storage.StorageSession;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Update storage action, this is a operation of data manipulating and supports manipulate
 * many instance in the same time.
 * @author javaito
 */
public abstract class Update<S extends StorageSession, A extends Object> extends ManipulationDataAction<S, A> {

    private final Object instance;
    private final Queryable queryable;

    public Update(S session, Class modelClass, Object instance, Queryable queryable) {
        super(session, modelClass);
        Objects.requireNonNull(instance, "Unsupported null instances");
        this.instance = instance;
        this.queryable = queryable;
    }

    public Update(S session, Object instance, Queryable queryable) {
        this(session, Map.class, instance, queryable);
    }

    public Update(S session, Class modelClass, Object instance) {
        this(session, modelClass, instance, null);
    }

    public Update(S session, Object instance) {
        this(session, Map.class, instance, null);
    }

    /**
     * Returns the instance that contains the information to update the instances.
     * @return Object that contains the new values.
     */
    @Override
    protected Collection<Object> getInstances() {
        return List.of(instance);
    }

    /**
     * Returns the queryable that indicate the instance to update.
     * @return Queryable instance.
     */
    public Queryable getQueryable() {
        return queryable;
    }
}
