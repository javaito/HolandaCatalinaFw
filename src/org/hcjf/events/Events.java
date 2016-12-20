package org.hcjf.events;

import org.hcjf.errors.Errors;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the event service for the instance.
 * @author javaito
 * @email javaito@gmail.com
 */
public final class Events extends Service<EventListener> {

    private static final Events instance;

    static {
        instance = new Events();
    }

    public final List<EventListener> listeners;

    private Events() {
        super(SystemProperties.get(SystemProperties.EVENT_SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.EVENT_SERVICE_PRIORITY));
        listeners = new ArrayList<>();
    }

    /**
     * Register a new event listener to the service.
     * @param consumer Event listener.
     */
    @Override
    public void registerConsumer(EventListener consumer) {
        if(consumer == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_EVENTS_1));
        }

        synchronized (listeners) {
            listeners.add(consumer);
        }
    }

    /**
     * Unregister a event listener to the service.
     * @param consumer Event listener.
     */
    @Override
    public void unregisterConsumer(EventListener consumer) {
        if(consumer == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_EVENTS_1));
        }

        synchronized (listeners) {
            listeners.remove(consumer);
        }
    }

    /**
     * Return all the listeners to be able of process the specific event.
     * @param event Event to dispatch.
     * @return List of listeners.
     */
    private List<EventListener> getListeners(Event event) {
        List<EventListener> result = new ArrayList<>();
        synchronized (listeners) {
            for(EventListener listener : listeners) {
                if(listener.getEventType().isAssignableFrom(event.getClass())) {
                    result.add(listener);
                }
            }
        }
        return result;
    }

    /**
     * Dispatch the event to all the listeners.
     * @param event Event to dispatch.
     */
    private void dispatchEvent(Event event) {
        if(event != null) {
            for (EventListener listener : getListeners(event)) {
                fork(() -> listener.onEventReceive(event));
            }
        }
    }

    /**
     * Send event.
     * @param event Event to send.
     */
    public static void sendEvent(Event event) {
        instance.dispatchEvent(event);
    }

    /**
     * Add event listener.
     * @param eventListener Event listener.
     */
    public static void addEventListener(EventListener eventListener) {
        instance.registerConsumer(eventListener);
    }

    /**
     * Remove event listener.
     * @param eventListener Event listener.
     */
    public static void removeEventListener(EventListener eventListener) {
        instance.unregisterConsumer(eventListener);
    }

}
