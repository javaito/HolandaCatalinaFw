package org.hcjf.layers.storage.actions;

import org.hcjf.layers.query.Queryable;
import org.hcjf.layers.storage.StorageSession;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Delete storage operation, this is a operation of data manipulating and supports manipulate
 * many instance in the same time.
 * @author javaito
 */
public abstract class Delete<S extends StorageSession, A extends Object> extends ManipulationDataAction<S, A> {

    private final Queryable queryable;

    public Delete(S session, Class modelClass, Queryable queryable) {
        super(session, modelClass);
        Objects.requireNonNull(queryable, "Unsupported null queryable");
        this.queryable = queryable;
    }

    public Delete(S session, Queryable queryable) {
        this(session, Map.class, queryable);
    }

    /**
     * Returns a list with the queryable instance to execute the delete.
     * @return Queryable instance.
     */
    @Override
    protected Collection<Object> getInstances() {
        return List.of(queryable);
    }

}
