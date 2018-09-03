package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

import java.util.Map;
import java.util.Objects;

/**
 * Insert storage operation.
 * @author javaito
 *
 */
public abstract class Insert<S extends StorageSession> extends StorageAction<S> {

    private Object instance;

    public Insert(S session, Class modelClass, Object instance) {
        super(session, modelClass);
        Objects.requireNonNull(instance, "Unsupported null instances");
        this.instance = instance;
    }

    public Insert(S session, Object instance) {
        this(session, Map.class, instance);
    }

    @Override
    public <R> ResultSet<R> execute() {

        return null;
    }

    protected abstract Object adaptObject(Object instance);

    protected abstract Object executeInsert(Object adaptedInstance);


}
