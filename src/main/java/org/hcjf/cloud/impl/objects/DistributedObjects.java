package org.hcjf.cloud.impl.objects;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 */
public class DistributedObjects {

    private final Map<String, DistributedMap> distributedMaps;

    public DistributedObjects() {
        distributedMaps = new HashMap<>();
    }


}
