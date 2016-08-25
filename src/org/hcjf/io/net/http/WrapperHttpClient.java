package org.hcjf.io.net.http;

import org.hcjf.io.net.NetService;
import org.hcjf.properties.SystemProperties;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a wrapper to mapping some response body with
 * some class of the implementation domain.
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class WrapperHttpClient extends HttpClient {

    private final Map<String, String> mapping;

    public WrapperHttpClient(URL url) {
        super(url);
        this.mapping = new HashMap<>();
    }

    /**
     *
     * @return
     */
    protected Map<String, String> getMapping() {
        return mapping;
    }

    /**
     * Add mapping between the response body and the domain class field.
     * @param sourceName Response body field.
     * @param destName Domain class field.
     */
    public final void addMapping(String sourceName, String destName) {
        mapping.put(sourceName, destName);
    }

    /**
     *
     * @param resourceClass
     * @param <O>
     * @return
     */
    public abstract <O extends Object> O getResource(Class<? extends O> resourceClass);

    /**
     *
     * @param resource
     * @param <O>
     * @return
     */
    public abstract <O extends Object> O completeResource(O resource);
}
