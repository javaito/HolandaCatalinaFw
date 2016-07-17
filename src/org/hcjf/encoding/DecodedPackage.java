package org.hcjf.encoding;

import org.hcjf.layers.query.Query;

import java.util.Map;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class DecodedPackage {

    private final Object object;
    private final Query query;
    private final Map<String, Object> parameters;

    public DecodedPackage(Object object, Map<String, Object> parameters) {
        if(object instanceof Query) {
            this.object = null;
            this.query = (Query) object;
        } else {
            this.object = object;
            this.query = null;
        }
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
