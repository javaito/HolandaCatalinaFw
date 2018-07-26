package org.hcjf.cloud.impl.messages;

import org.hcjf.events.DistributedEvent;
import org.hcjf.io.net.messages.Message;

import java.util.UUID;

/**
 * @author javaito
 */
public class EventMessage extends Message {

    public DistributedEvent event;

    public EventMessage() {
    }

    public EventMessage(UUID id) {
        super(id);
    }

    public DistributedEvent getEvent() {
        return event;
    }

    public void setEvent(DistributedEvent event) {
        this.event = event;
    }
}
