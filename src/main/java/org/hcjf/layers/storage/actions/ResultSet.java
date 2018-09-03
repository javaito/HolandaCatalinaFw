package org.hcjf.layers.storage.actions;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This object is the result for all the queries executed using the storage layer.
 * This object is basically an wrapper of a collection that is configured into the constructor.
 * @author javaito
 */
public class ResultSet<O extends Object> implements Collection<O> {

    private final Collection<O> result;
    private final Integer affectedInstances;
    private final Long adaptationTime;
    private final Long executionTime;
    private final Long presentationTime;
    private final Long validationIntegrityTime;
    private final Long totalTime;

    public ResultSet(Collection<O> result, Integer affectedInstances, Long adaptationTime, Long executionTime,
                     Long presentationTime, Long validationIntegrityTime, Long totalTime) {
        this.result = result;
        this.affectedInstances = affectedInstances;
        this.adaptationTime = adaptationTime;
        this.executionTime = executionTime;
        this.presentationTime = presentationTime;
        this.validationIntegrityTime = validationIntegrityTime;
        this.totalTime = totalTime;
    }

    /**
     * Returns the number of affected storage instances after the execution.
     * @return Number of the affected storage instances.
     */
    public Integer getAffectedInstances() {
        return affectedInstances;
    }

    /**
     * Returns the time consumed adapting the original query.
     * @return Adaptation time.
     */
    public Long getAdaptationTime() {
        return adaptationTime;
    }

    /**
     * Returns the time consumed executing the action on the underlying technology.
     * @return Execution time.
     */
    public Long getExecutionTime() {
        return executionTime;
    }

    /**
     * Returns the time consumed transforming the raw information to the expected presentation format.
     * @return Presentation time.
     */
    public Long getPresentationTime() {
        return presentationTime;
    }

    /**
     * Returns the time consumed validating the data integrity.
     * @return Validation integrity time.
     */
    public Long getValidationIntegrityTime() {
        return validationIntegrityTime;
    }

    /**
     * Returns the total time consumed performing the operation.
     * @return Total time.
     */
    public Long getTotalTime() {
        return totalTime;
    }

    @Override
    public int size() {
        return result.size();
    }

    @Override
    public boolean isEmpty() {
        return result.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return result.contains(o);
    }

    @Override
    public Iterator<O> iterator() {
        return result.iterator();
    }

    @Override
    public Object[] toArray() {
        return result.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return result.toArray(a);
    }

    @Override
    public boolean add(O o) {
        throw new UnsupportedOperationException("the result set is a unmodifiable instance of collection");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("the result set is a unmodifiable instance of collection");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return result.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends O> c) {
        return result.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return result.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super O> filter) {
        return result.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return result.retainAll(c);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("the result set is a unmodifiable instance of collection");
    }

    @Override
    public boolean equals(Object o) {
        return result.equals(o);
    }

    @Override
    public int hashCode() {
        return result.hashCode();
    }

    @Override
    public Spliterator<O> spliterator() {
        return result.spliterator();
    }

    @Override
    public Stream<O> stream() {
        return result.stream();
    }

    @Override
    public Stream<O> parallelStream() {
        return result.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super O> action) {
        result.forEach(action);
    }
}
