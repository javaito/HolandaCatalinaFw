package org.hcjf.cloud.impl.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author javaito
 */
public class DistributedTree implements DistributedObject {

    private final String name;
    private final Map<String, DistributedObject> branchs;

    public DistributedTree(String name) {
        this.name = name;
        this.branchs = new HashMap<>();
    }

    @Override
    public final String getName() {
        return name;
    }

    public final void add(DistributedObject distributedObject) {
        Objects.requireNonNull(distributedObject, "Null distributed object");
        branchs.put(distributedObject.getName(), distributedObject);
    }

    @Override
    public Object getInstance() {
        return branchs;
    }

    public Object getInstance(String... path) {
        return getInstance(0, path);
    }

    private Object getInstance(int index, String... path) {
        Object result = null;
        DistributedObject distributedObject = branchs.get(path[index++]);
        if(distributedObject != null) {
            if (index == path.length) {
                result = distributedObject.getInstance();
            } else if(distributedObject instanceof DistributedTree) {
                result = ((DistributedTree)distributedObject).getInstance(index, path);
            }
        }
        return result;
    }
}
