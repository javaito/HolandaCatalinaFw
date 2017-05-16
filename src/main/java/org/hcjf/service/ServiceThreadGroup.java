package org.hcjf.service;

/**
 * This class is a singleton and all the service thread
 * run over the unique instance of this class as a group.
 * @author javaito
 *
 */
public final class ServiceThreadGroup extends ThreadGroup {

    private static final String NAME = "ServiceThreadGroup";
    private static final ServiceThreadGroup instance;

    static {
        instance = new ServiceThreadGroup();
    }

    private ServiceThreadGroup() {
        super(NAME);
    }

    /**
     * Retur the unique instance of this class.
     * @return Unique instance of the group.
     */
    public static ServiceThreadGroup getInstance() {
        return instance;
    }
}
