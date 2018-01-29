package org.hcjf.events;

/**
 * This class only wrap an instance of distributed event in order to
 * prevent the object from being sent again.
 * @author javaito
 */
public final class RemoteEvent implements Event {

    private final DistributedEvent event;

    public RemoteEvent(DistributedEvent event) {
        this.event = event;
    }

    public DistributedEvent getEvent() {
        return event;
    }

    @Override
    public String getName() {
        return event.getName();
    }
}
