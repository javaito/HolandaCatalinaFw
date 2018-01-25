package org.hcjf.cloud.impl.objects;

import java.util.*;

/**
 * @author javaito
 */
public class DistributedTree implements DistributedObject {

    private final Object key;
    private final Map<Object, DistributedObject> branches;
    private final Long lastUpdate;

    public DistributedTree(Object key) {
        this.key = key;
        this.branches = new HashMap<>();
        this.lastUpdate = System.currentTimeMillis();
    }

    @Override
    public final Object getKey() {
        return key;
    }

    @Override
    public final Long getLastUpdate() {
        return lastUpdate;
    }

    public final int size() {
        return branches.size();
    }

    public final boolean isEmpty() {
        return branches.isEmpty();
    }

    public final boolean containsKey(Object key) {
        return branches.containsKey(key);
    }

    public final Set keySet() {
        return branches.keySet();
    }

    public final synchronized LocalLeaf add(Object object, Long timestamp, Object... path) {
        Objects.requireNonNull(object, "Null distributed object");
        LocalLeaf result;
        createPath(0, path.length - 1, path);
        Object instance = getInstance(0, path.length - 1, path);
        if(instance instanceof DistributedTree) {
            Object key = path[path.length-1];
            result = new LocalLeaf(key);
            result.setLastUpdate(lastUpdate);
            result.setInstance(object);

            DistributedLeaf leaf = (DistributedLeaf) branches.get(key);
            if(leaf != null) {
                if(leaf.getLastUpdate() < timestamp) {
                    ((DistributedTree) instance).branches.put(key, result);
                }
            } else {
                ((DistributedTree) instance).branches.put(key, result);
            }
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    public final synchronized RemoteLeaf add(Long timestamp, List<UUID> nodes, Object... path) {
        RemoteLeaf result;
        createPath(0, path.length - 1, path);
        Object instance = getInstance(0, path.length - 1, path);
        if(instance instanceof DistributedTree) {
            Object key = path[path.length-1];
            result = new RemoteLeaf(key);
            result.setLastUpdate(lastUpdate);
            RemoteLeaf.RemoteValue remoteValue = new RemoteLeaf.RemoteValue();
            remoteValue.setNodes(nodes);
            result.setRemoteValue(remoteValue);

            DistributedLeaf leaf = (DistributedLeaf) branches.get(key);
            if(leaf != null) {
                if(leaf.getLastUpdate() < timestamp) {
                    ((DistributedTree) instance).branches.put(key, result);
                }
            } else {
                ((DistributedTree) instance).branches.put(key, result);
            }
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    public final synchronized DistributedObject remove(Object... path) {
        return remove(0, path);
    }

    private DistributedObject remove(int index, Object... path) {
        DistributedObject result = null;
        if(index + 1 == path.length) {
            result = branches.remove(path[index]);
        } else {
            DistributedObject distributedObject = branches.get(index++);
            if(distributedObject != null && distributedObject instanceof DistributedTree) {
                result = remove(index, path);
            }
        }
        return result;
    }

    public final synchronized void clear(Object... path) {
        clear(0, path);
    }

    private void clear(int index, Object... path) {
        DistributedObject distributedObject = branches.get(index++);
        if(distributedObject instanceof DistributedTree) {
            if(index == path.length) {
                ((DistributedTree) distributedObject).branches.clear();
            } else {
                ((DistributedTree) distributedObject).clear(index, path);
            }
        }
    }

    @Override
    public Object getInstance() {
        return this;
    }

    public Object getInstance(Object... path) {
        return getInstance(0, path.length, path);
    }

    private Object getInstance(int index, int length, Object... path) {
        Object result = null;
        DistributedObject distributedObject = branches.get(path[index++]);
        if(distributedObject != null) {
            if (index == length) {
                result = distributedObject.getInstance();
            } else if(distributedObject instanceof DistributedTree) {
                result = ((DistributedTree)distributedObject).getInstance(index, length, path);
            }
        }
        return result;
    }

    public synchronized boolean createPath(Object... path) {
        return createPath(0, path.length, path);
    }

    private boolean createPath(int index, int length, Object... path) {
        boolean result = false;
        Object key = path[index++];
        if(!branches.containsKey(key)) {
            branches.put(key, new DistributedTree(key));
            result = true;
        }

        if(index < length) {
            result = result & ((DistributedTree)branches.get(key)).createPath(index, length, path);
        }

        return result;
    }
}
