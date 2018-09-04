package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Insert storage operation.
 * @author javaito
 */
public abstract class Insert<S extends StorageSession> extends StorageAction<S> {

    private static final String ADAPTING_STEP_EXCEPTION = "Adapting step fail in %s resolving insert data %s";
    private static final String EXECUTING_STEP_EXCEPTION = "Executing step fail in %s resolving insert data %s";

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
     * The insert implementation has two steps to insert the instance.
     * - First, adapt the instance to obtain the format to insert the instance into the underlying technology.
     * - Second, execute the insert into the underlying technology.
     * @param <R> Expected return data type.
     * @return Returns a result set instance with the instance inserted.
     */
    @Override
    public <R> ResultSet<R> execute() {
        Object adaptedInstance;
        R insertedInstance;

        //Initializing time counter.
        Long startTime = System.currentTimeMillis();

        //Adapting instance to inset
        try {
            adaptedInstance = adaptObject(instance);
        } catch (Exception ex){
            throw new RuntimeException(String.format(ADAPTING_STEP_EXCEPTION, getClass().getName(), instance.toString()), ex);
        }
        Long adaptationTime = System.currentTimeMillis() - startTime;

        //Execute insert
        try {
            insertedInstance = executeInsert(adaptedInstance);
        } catch (Exception ex) {
            throw new RuntimeException(String.format(EXECUTING_STEP_EXCEPTION, getClass().getName(), instance.toString()), ex);
        }
        Long executionTime = System.currentTimeMillis() - adaptationTime;
        Long totalTime = System.currentTimeMillis() - startTime;

        return new ResultSet(List.of(insertedInstance), 1, adaptationTime, executionTime,
                0L, 0L, totalTime);
    }

    /**
     * Returns a object created from the original instance that contains
     * all the elements to execute the insert into the underlying technology.
     * @param instance Original instance.
     * @return Adapted instance.
     */
    protected abstract Object adaptObject(Object instance);

    /**
     * Execute the operation on the underlying technology using the adapted instance.
     * @param adaptedInstance Adapted instance.
     * @param <R> Expected return data type.
     * @return Inserted instance.
     */
    protected abstract <R> R executeInsert(Object adaptedInstance);


}
