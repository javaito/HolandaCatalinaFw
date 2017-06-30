package org.hcjf.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author javaito
 */
public class TtlSet<V extends Object> extends TtlCollection<V> implements Set<V> {

    private final Set<V> instance;

    public TtlSet(Set<V> instance, Long timeWindowsSize) {
        super(timeWindowsSize);
        this.instance = instance;
    }

    @Override
    protected void removeOldInstance(V instanceKey) {
        instance.remove(instanceKey);
    }

    @Override
    public int size() {
        removeOldWindows();
        return instance.size();
    }

    @Override
    public boolean isEmpty() {
        removeOldWindows();
        return instance.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        removeOldWindows();
        return instance.contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        removeOldWindows();
        return instance.iterator();
    }

    @Override
    public Object[] toArray() {
        removeOldWindows();
        return instance.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        removeOldWindows();
        return instance.toArray(a);
    }

    @Override
    public boolean add(V v) {
        removeOldWindows();
        addInstance(v);
        return instance.add(v);
    }

    @Override
    public boolean remove(Object o) {
        removeOldWindows();
        return instance.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        removeOldWindows();
        return instance.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        c.forEach(this::remove);
        return true;
    }

    @Override
    public void clear() {
        instance.clear();
    }

    @Override
    public boolean equals(Object o) {
        return instance.equals(o);
    }

    @Override
    public int hashCode() {
        return instance.hashCode();
    }

    @Override
    public Spliterator<V> spliterator() {
        removeOldWindows();
        return instance.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super V> filter) {
        removeOldWindows();
        return instance.removeIf(filter);
    }

    @Override
    public Stream<V> stream() {
        removeOldWindows();
        return instance.stream();
    }

    @Override
    public Stream<V> parallelStream() {
        removeOldWindows();
        return instance.parallelStream();
    }
}
