package org.hcjf.events;

/**
 * This class implements the system event structure.
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class Event {

    private final String name;

    public Event(String name) {
        this.name = name;
    }

    /**
     * Return the name of the event.
     * @return Event name.
     */
    public final String getName() {
        return name;
    }

}
