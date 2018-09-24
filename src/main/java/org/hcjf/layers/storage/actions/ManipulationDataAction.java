package org.hcjf.layers.storage.actions;

import org.hcjf.layers.storage.StorageSession;

import java.util.*;

/**
 * @author javaito
 */
public abstract class ManipulationDataAction<S extends StorageSession, A extends Object> extends StorageAction<S> {

    private static final String ADAPTING_STEP_EXCEPTION = "Adapting step fail in %s updating data %s";
    private static final String EXECUTING_STEP_EXCEPTION = "Executing step fail in %s updating data %s";

    public ManipulationDataAction(S session, Class modelClass) {
        super(session, modelClass);
    }

    public ManipulationDataAction(S session) {
        this(session, Map.class);
    }

    /**
     * Returns the collection with all the instance to process.
     * @return Collection with all the instance to process.
     */
    protected abstract Collection<Object> getInstances();

    /**
     * The insert implementation has two steps to insert the instance.
     * - First, adapt the instance to obtain the format to insert the instance into the underlying technology.
     * - Second, execute the insert into the underlying technology.
     * @param <R> Expected return data type.
     * @return Returns a result set instance with the instance inserted.
     */
    @Override
    public <R> ResultSet<R> execute() {
        A adaptedInstance;
        Collection<R> resultSet = new ArrayList<>();

        //Initializing time counter.
        Long startTime = System.currentTimeMillis();
        Long adaptationTime = 0L;
        Long executionTime = 0L;

        for(Object instance : getInstances()) {
            //Adapting instance to inset
            try {
                adaptedInstance = adaptObject(instance);
            } catch (Exception ex) {
                throw new RuntimeException(String.format(ADAPTING_STEP_EXCEPTION, getClass().getName(), instance.toString()), ex);
            }
            adaptationTime += System.currentTimeMillis() - startTime;

            //Execute insert
            try {
                resultSet.add(execute(adaptedInstance));
            } catch (Exception ex) {
                throw new RuntimeException(String.format(EXECUTING_STEP_EXCEPTION, getClass().getName(), instance.toString()), ex);
            }
            executionTime = System.currentTimeMillis() - adaptationTime;
        }

        Long totalTime = System.currentTimeMillis() - startTime;

        return new ResultSet(resultSet, resultSet.size(), adaptationTime, executionTime,
                0L, 0L, totalTime);
    }

    /**
     * Returns a object created from the original instance that contains
     * all the elements to execute the insert into the underlying technology.
     * @param instance Original instance.
     * @return Adapted instance.
     */
    protected abstract A adaptObject(Object instance);

    /**
     * Execute the operation on the underlying technology using the adapted instance.
     * @param adaptedInstance Adapted instance.
     * @param <R> Expected return data type.
     * @return Inserted instance.
     */
    protected abstract <R> R execute(A adaptedInstance);
}
