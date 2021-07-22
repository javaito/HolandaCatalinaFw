package org.hcjf.layers.query.model;

import org.hcjf.layers.query.JoinableMap;
import org.hcjf.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class QueryJsonResource extends QueryResource {

    private static final String TO_STRING_PATTERN = "'%s' as %s";

    private final Collection<JoinableMap> resourceValues;

    public QueryJsonResource(String resourceName, Collection<Map<String,Object>> resourceValues) {
        super(resourceName);
        this.resourceValues = new ArrayList<>();
        for(Map<String,Object> value : resourceValues) {
            JoinableMap joinableMap = new JoinableMap(resourceName);
            joinableMap.setResource(resourceName);
            joinableMap.putAll(value);
            this.resourceValues.add(joinableMap);
        }
    }

    public Collection<JoinableMap> getResourceValues() {
        return resourceValues;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_PATTERN, JsonUtils.toJsonTree(resourceValues).toString(), super.toString());
    }
}
