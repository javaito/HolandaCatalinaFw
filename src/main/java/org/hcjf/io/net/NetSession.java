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
    private boolean checked;

    public NetSession(UUID id, NetServiceConsumer consumer) {
        super(id);
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
     * Return a value to indicate if the session is checked.
     * @return True if the session is checked and false in otherwise
     */
    public final boolean isChecked() {
        return checked;
    }

    /**
     * Set a value to indicate if the session is chacked.
     * @param checked Checked value.
     */
    public final void setChecked(boolean checked) {
        this.checked = checked;
    }
}
