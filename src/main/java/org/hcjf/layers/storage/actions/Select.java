package org.hcjf.layers.storage.actions;

import org.hcjf.layers.query.Queryable;
import org.hcjf.layers.storage.StorageSession;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Select storage operation.
 * @author javaito
 */
public abstract class Select<S extends StorageSession, O extends Object> extends StorageAction<S> {

    private static final String ADAPTING_STEP_EXCEPTION = "Adapting step fail in %s resolving queryable %s";
    private static final String EXECUTING_STEP_EXCEPTION = "Executing step fail in %s resolving queryable %s";
    private static final String PRESENTATION_STEP_EXCEPTION = "Presentation step fail in %s resolving queryable %s";
    private static final String VALIDATING_INTEGRITY_EXCEPTION = "Validation integrity step fail in %s resolving queryable %s";

    private final Queryable queryable;

    public Select(S session, Class modelClass, Queryable queryable) {
        super(session, modelClass);
        Objects.requireNonNull(queryable, "Unsupported null queryable");
        this.queryable = queryable;
    }

    public Select(S session, Queryable queryable) {
        this(session, Map.class, queryable);
    }

    protected final Queryable getQueryable() {
        return queryable;
    }

    /**
     * The select implementation has 4 steps to obtain the result set.
     * - First, adapt the query in order to create a new query executable for the underlying technology.
     * - Second, call the underlying technology in order to obtain the raw information.
     * - Third, create a collection of data in the presentation format using the row data.
     * - Fourth, Validate the integrity of the result set using the original query.
     * As result of these four step, the method returns a instance of result set.
     * @param <R> Expected result set data.
     * @return Result set instance.
     */
    @Override
    public <R> ResultSet<R> execute() {
        Queryable adaptedQueryable;
        O executionResult;
        Collection<R> collection;

        //Initializing time counter.
        Long startTime = System.currentTimeMillis();

        //Adapting the original query to call the underlying technology
        try {
            adaptedQueryable = adaptQuery(getQueryable());
        } catch (Exception ex) {
            throw new RuntimeException(String.format(ADAPTING_STEP_EXCEPTION, getClass().getName(), getQueryable().toString()), ex);
        }
        Long adaptationTime = System.currentTimeMillis() - startTime;

        //Calling the underlying technology
        try {
            executionResult = executeQuery(adaptedQueryable);
        } catch (Exception ex) {
            throw new RuntimeException(String.format(EXECUTING_STEP_EXCEPTION, getClass().getName(), getQueryable().toString()), ex);
        }
        Long executionTime = System.currentTimeMillis() - adaptationTime;

        //Creating the collection with all the information in the presentation format.
        try {
            collection = createResultSet(executionResult);
        } catch (Exception ex) {
            throw new RuntimeException(String.format(PRESENTATION_STEP_EXCEPTION, getClass().getName(), getQueryable().toString()), ex);
        }
        Long presentationTime = System.currentTimeMillis() - executionTime;

        //Checking the integrity of the result set.
        try {
            collection = getQueryable().evaluate(collection);
        } catch (Exception ex) {
            throw new RuntimeException(String.format(VALIDATING_INTEGRITY_EXCEPTION, getClass().getName(), getQueryable().toString()), ex);
        }
        Long validationIntegrityTime = System.currentTimeMillis() - presentationTime;

        Long totalTime = System.currentTimeMillis() - startTime;
        return new ResultSet(collection, 0, adaptationTime, executionTime,
                presentationTime, validationIntegrityTime, totalTime);
    }

    /**
     * This method must adapt the original query and create a new query supported by the underlying technology.
     * @param queryable Original query.
     * @return Adapted query.
     */
    protected abstract Queryable adaptQuery(Queryable queryable);

    /**
     * This method execute the adapted query instance on the underlying technology and returns the result set instance
     * provided for that technology.
     * @param queryable Adapted query obtained into the previous step.
     * @return Result set.
     */
    protected abstract O executeQuery(Queryable queryable);

    /**
     * Creates the expected data type using the raw data obtained from the underlying technology.
     * @param executionResult Object that represent the result set obtained in the previous step.
     * @param <R> Expected data type.
     * @return Collection with all the instances created from the raw data.
     */
    protected abstract <R> Collection<R> createResultSet(O executionResult);
}
