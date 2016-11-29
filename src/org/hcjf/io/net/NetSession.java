package org.hcjf.io.net;

import org.hcjf.service.ServiceSession;

import java.util.UUID;

/**
 * This interface define de behavior of the net session.
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class NetSession extends ServiceSession {

    private final NetServiceConsumer consumer;
    private boolean locked;

    public NetSession(UUID id, String sessionName, NetServiceConsumer consumer) {
        super(id, sessionName);
        this.consumer = consumer;
        this.locked = false;
    }

    /**
     * Return the consumer.
     * @return Consumer.
     */
    public NetServiceConsumer getConsumer() {
        return consumer;
    }

    /**
     * Lock session.
     */
    public final void lock() {
        locked = true;
    }

    /**
     * Unlock session.
     */
    public final void unlock() {
        locked = false;
    }

    /**
     * Return the lock status of the session.
     * @return True if the session is locked and false otherwise
     */
    public final boolean isLocked() {
        return locked;
    }

    /**
     * Add system use time to specific session.
     * @param time System use time.
     */
    public void addThreadTime(long time){
    }

}
