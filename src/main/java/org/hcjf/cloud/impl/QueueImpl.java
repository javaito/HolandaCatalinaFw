package org.hcjf.cloud.impl;

import org.hcjf.cloud.impl.network.CloudOrchestrator;
import org.hcjf.cloud.impl.objects.DistributedTree;

import java.util.*;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public class QueueImpl<O extends Object> implements Queue<O> {

    private final String name;

    public QueueImpl(String name) {
        this.name = name;
        CloudOrchestrator.getInstance().publishPath(Queue.class.getName(), name);
    }

    @Override
    public int size() {
        DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
        return tree.size();
    }

    @Override
    public boolean isEmpty() {
        DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
        return tree.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
        Boolean result = false;
        for(Object key : tree.keySet()) {
            result = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name, key).equals(o);
            if(result) {
                break;
            }
        }
        return result;
    }

    @Override
    public Iterator<O> iterator() {
        DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
        return new Iterator<>() {

            Set keySet = Set.of(tree.keySet());
            Iterator iterator = keySet.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public O next() {
                return CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name, iterator.next());
            }
        };
    }

    @Override
    public Object[] toArray() {
        DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
        Object[] result = new Object[tree.size()];
        int i = 0;
        for(Object key : tree.keySet()) {
            result[i++] = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name, key);
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) toArray();
    }

    @Override
    public boolean add(O value) {
        CloudOrchestrator.getInstance().publishObject(value, System.currentTimeMillis(), Queue.class.getName(), name, createKey());
        return true;
    }

    @Override
    public boolean remove(Object o) {
        DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
        Boolean result = false;
        for(Object key : tree.keySet()) {
            result = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name, key).equals(o);
            CloudOrchestrator.getInstance().hidePath(Queue.class.getName(), name, key);
            if(result) {
                break;
            }
        }
        return result;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean result = true;
        for(Object object : c) {
            result &= contains(object);
            if(!result) {
                break;
            }
        }
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends O> c) {
        boolean result = true;
        for(O object : c) {
            result &= add(object);
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = true;
        for(Object object : c) {
            result &= remove(object);
        }
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean result = false;
        for(Object object : c) {
            if(!contains(object)) {
                remove(object);
                result = true;
            }
        }
        return result;
    }

    @Override
    public void clear() {
        DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
        for(Object key : tree.keySet()) {
            CloudOrchestrator.getInstance().hidePath(Queue.class.getName(), name, key);
        }
    }

    @Override
    public boolean offer(O o) {
        return add(o);
    }

    @Override
    public O remove() {
        if(size() > 0) {
            O result;
            DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
            TreeSet keys = new TreeSet();
            keys.addAll(tree.keySet());
            result = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name, keys.first());
            CloudOrchestrator.getInstance().hidePath(Queue.class.getName(), name, keys.first());
            return result;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public O poll() {
        O result = null;
        if(size() > 0) {
            DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
            TreeSet keys = new TreeSet();
            keys.addAll(tree.keySet());
            result = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name, keys.first());
            CloudOrchestrator.getInstance().hidePath(Queue.class.getName(), name, keys.first());
        }
        return result;
    }

    @Override
    public O element() {
        if(size() > 0) {
            O result;
            DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
            TreeSet keys = new TreeSet();
            keys.addAll(tree.keySet());
            result = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name, keys.first());
            return result;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public O peek() {
        O result = null;
        if(size() > 0) {
            DistributedTree tree = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name);
            TreeSet keys = new TreeSet();
            keys.addAll(tree.keySet());
            result = CloudOrchestrator.getInstance().invokeNode(Queue.class.getName(), name, keys.first());
        }
        return result;
    }

    private String createKey() {
        return Long.toString(System.currentTimeMillis()) + Long.toString(System.nanoTime());
    }

}
