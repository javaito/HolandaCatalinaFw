package org.hcjf.cloud.impl.objects;

/**
 * @author javaito
 */
public interface DistributedObject {

    Object getKey();

    Object getInstance();

    Long getLastUpdate();

}
