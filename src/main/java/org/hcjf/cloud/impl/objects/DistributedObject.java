package org.hcjf.cloud.impl.objects;

/**
 * @author javaito
 */
public abstract class DistributedObject {

    private final String name;

    public DistributedObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
