package org.hcjf.encoding;

import java.util.Map;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class DecodedPackage {

    private final Object object;
    private final Map<String, Object> parameters;

    public DecodedPackage(Object object, Map<String, Object> parameters) {
        this.object = object;
        this.parameters = parameters;
    }

    public DecodedPackage(Map<String, Object> parameters) {
        this(null, parameters);
    }

    /**
     *
     * @return
     */
    public Object getObject() {
        return object;
    }

    /**
     *
     * @return
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
}
