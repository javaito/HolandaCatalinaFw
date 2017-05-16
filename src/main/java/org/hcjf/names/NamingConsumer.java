package org.hcjf.names;

import org.hcjf.service.ServiceConsumer;

/**
 * @author javaito
 *
 */
public abstract class NamingConsumer implements ServiceConsumer {

    private final String name;

    public NamingConsumer(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public final String getName() {
        return name;
    }

    /**
     *
     * @param value
     * @return
     */
    public abstract String normalize(String value);
}
