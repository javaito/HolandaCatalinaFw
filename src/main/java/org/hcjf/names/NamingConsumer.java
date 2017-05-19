package org.hcjf.names;

import org.hcjf.service.ServiceConsumer;

/**
 * Naming service consumer base class.
 * @author javaito
 */
public abstract class NamingConsumer implements ServiceConsumer {

    private final String name;

    public NamingConsumer(String name) {
        this.name = name;
    }

    /**
     * Return the name of the consumer.
     * @return Name of the consumer.
     */
    public final String getName() {
        return name;
    }

    /**
     * Normalize the specific value.
     * @param value Value to normalize.
     * @return Normalized value.
     */
    public abstract String normalize(String value);
}
