package org.hcjf.cloud.impl.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author javaito
 */
public class DistributedTree implements DistributedObject {

    private final String name;
    private final Map<String, DistributedObject> branches;
    private final Long lastUpdate;

    public DistributedTree(String name) {
        this.name = name;
        this.branches = new HashMap<>();
        this.lastUpdate = System.currentTimeMillis();
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Long getLastUpdate() {
        return lastUpdate;
    }

    public final LocalLeaf add(Object object, Long timestamp, String... path) {
        Objects.requireNonNull(object, "Null distributed object");
        LocalLeaf result;
        createPath(0, path.length - 1, path);
        Object instance = getInstance(0, path.length - 1, path);
        if(instance instanceof DistributedTree) {
            String name = path[path.length-1];
            result = new LocalLeaf(name);
            result.setLastUpdate(lastUpdate);
            result.setInstance(object);

            DistributedLeaf leaf = (DistributedLeaf) branches.get(name);
            if(leaf != null) {
                if(leaf.getLastUpdate() < timestamp) {
                    ((DistributedTree) instance).branches.put(name, result);
                }
            } else {
                ((DistributedTree) instance).branches.put(name, result);
            }
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    public final RemoteLeaf add(Long timestamp, String... path) {
        RemoteLeaf result;
        createPath(0, path.length - 1, path);
        Object instance = getInstance(0, path.length - 1, path);
        if(instance instanceof DistributedTree) {
            String name = path[path.length-1];
            result = new RemoteLeaf(name);
            result.setLastUpdate(lastUpdate);

            DistributedLeaf leaf = (DistributedLeaf) branches.get(name);
            if(leaf != null) {
                if(leaf.getLastUpdate() < timestamp) {
                    ((DistributedTree) instance).branches.put(name, result);
                }
            } else {
                ((DistributedTree) instance).branches.put(name, result);
            }
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    @Override
    public Object getInstance() {
        return this;
    }

    public Object getInstance(String... path) {
        return getInstance(0, path.length, path);
    }

    private Object getInstance(int index, int length, String... path) {
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

    public synchronized void createPath(String... path) {
        createPath(0, path.length, path);
    }

    private void createPath(int index, int length, String... path) {
        String name = path[index++];
        if(!branches.containsKey(name)) {
            branches.put(name, new DistributedTree(name));
        }
        if(index < length) {
            createPath(index, length, path);
        }
    }
}
