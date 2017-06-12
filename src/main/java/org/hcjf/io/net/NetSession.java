package org.hcjf.io.net;

import org.hcjf.service.ServiceSession;

import java.util.UUID;

/**
 * This interface define de behavior of the net session.
 * @author javaito
 */
public abstract class NetSession extends ServiceSession {

    private final NetServiceConsumer consumer;
    private boolean checked;

    public NetSession(UUID id, NetServiceConsumer consumer) {
        super(id);
        this.consumer = consumer;
    }

    /**
     * Return the consumer.
     * @return Consumer.
     */
    public NetServiceConsumer getConsumer() {
        return consumer;
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
