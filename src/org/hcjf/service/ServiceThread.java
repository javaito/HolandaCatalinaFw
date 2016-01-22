package org.hcjf.service;

import java.util.Random;
import java.util.UUID;

/**
 * This are the thread created by the factory in the
 * class service, all the services run over this kind of
 * thread.
 * @author javaito
 * @email javaito@gmail.com
 */
public class ServiceThread extends Thread {

    private static final String NAME = "ServiceThread";

    /**
     * Constructor of the service thread.
     * @param target Runnable objet with the custom task.
     */
    public ServiceThread(Runnable target) {
        super(ServiceThreadGroup.getInstance(), target, NAME + UUID.randomUUID().toString());
    }
}
