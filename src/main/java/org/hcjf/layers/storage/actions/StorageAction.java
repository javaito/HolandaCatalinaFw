package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class is the base class for all the possible operations
 * over data storage session.
 * @author javaito
 */
public abstract class StorageAction<S extends StorageSession> {

    private final S session;
    private final Class modelClass;

    public StorageAction(S session, Class modelClass) {
        Objects.requireNonNull(session, "Unsupported null session");
        Objects.requireNonNull(modelClass, "Unsupported null model class");
        this.session = session;
        this.modelClass = modelClass;
    }

    public StorageAction(S session) {
        this(session, Map.class);
    }

    /**
     * Returns the storage session instance over the operation was created.
     * @return Storage session instance.
     */
    protected final S getSession() {
        return session;
    }

    /**
     * Returns the class of the model that the action will use internally.
     * @return Model class.
     */
    protected final Class getModelClass() {
        return modelClass;
    }

    /**
     * This method implements the logic to execute the current operation.
     * @param <R> Expected return type.
     * @return Result set with the execution response.
     */
    public abstract <R extends Object> ResultSet<R> execute();
}
